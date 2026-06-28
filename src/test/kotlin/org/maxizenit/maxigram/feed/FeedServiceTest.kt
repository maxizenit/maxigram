package org.maxizenit.maxigram.feed

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.maxizenit.maxigram.profile.SubscriptionService
import org.mockito.Mockito
import java.time.Instant
import java.util.UUID

class FeedServiceTest {

    private val posts = FakePostRepository()
    private val subscriptions = Mockito.mock(SubscriptionService::class.java)
    private val service = FeedService(posts, subscriptions)

    private val alice = UUID.randomUUID()
    private val bob = UUID.randomUUID()

    @Test
    fun `feed contains posts from followed authors and self, newest first`() {
        posts.seed(Post(1, bob, "from bob", Instant.parse("2026-06-01T00:00:00Z")))
        posts.seed(Post(2, alice, "from me", Instant.parse("2026-06-02T00:00:00Z")))
        posts.seed(Post(3, UUID.randomUUID(), "stranger", Instant.parse("2026-06-03T00:00:00Z")))
        Mockito.`when`(subscriptions.authorsFollowedBy(alice)).thenReturn(listOf(bob))

        val feed = service.feedFor(alice)

        assertThat(feed.map { it.id }).containsExactly(2, 1)
    }
}
