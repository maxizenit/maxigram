package org.maxizenit.maxigram.profile

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.context.ApplicationEventPublisher
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID

class SubscriptionServiceTest {

    private val repository = FakeSubscriptionRepository()
    private val service =
        SubscriptionService(repository, ApplicationEventPublisher { }, Clock.fixed(Instant.EPOCH, ZoneOffset.UTC))

    private val alice = UUID.randomUUID()
    private val bob = UUID.randomUUID()

    @Test
    fun `subscribing records the author`() {
        service.subscribe(alice, bob)

        assertThat(service.authorsFollowedBy(alice)).containsExactly(bob)
    }

    @Test
    fun `subscribing twice is idempotent`() {
        service.subscribe(alice, bob)
        service.subscribe(alice, bob)

        assertThat(service.authorsFollowedBy(alice)).containsExactly(bob)
    }

    @Test
    fun `cannot subscribe to yourself`() {
        assertThatThrownBy { service.subscribe(alice, alice) }
            .isInstanceOf(InvalidSubscriptionException::class.java)
    }

    @Test
    fun `unsubscribing removes the author`() {
        service.subscribe(alice, bob)
        service.unsubscribe(alice, bob)

        assertThat(service.authorsFollowedBy(alice)).isEmpty()
    }

    private class FakeSubscriptionRepository : SubscriptionRepository {
        private val edges = linkedSetOf<Pair<UUID, UUID>>()

        override fun insertIfAbsent(subscriberId: UUID, authorId: UUID, createdAt: Instant): Boolean =
            edges.add(subscriberId to authorId)

        override fun delete(subscriberId: UUID, authorId: UUID) {
            edges.remove(subscriberId to authorId)
        }

        override fun findAuthorIds(subscriberId: UUID): List<UUID> =
            edges.filter { it.first == subscriberId }.map { it.second }
    }
}
