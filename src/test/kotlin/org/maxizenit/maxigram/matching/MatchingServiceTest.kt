package org.maxizenit.maxigram.matching

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.maxizenit.maxigram.chat.Chat
import org.maxizenit.maxigram.chat.ChatService
import org.maxizenit.maxigram.feed.FeedActivityProvider
import org.maxizenit.maxigram.matching.activity.ActivityRepository
import org.maxizenit.maxigram.profile.Interest
import org.maxizenit.maxigram.profile.ProfileService
import org.maxizenit.maxigram.profile.UserProfile
import org.mockito.BDDMockito.given
import org.mockito.Mockito.mock
import org.springframework.context.ApplicationEventPublisher
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.UUID

class MatchingServiceTest {

    private val requests = FakeMatchRequestRepository()
    private val profileService = mock(ProfileService::class.java)
    private val feedActivity = mock(FeedActivityProvider::class.java)
    private val activity = mock(ActivityRepository::class.java)
    private val chatService = mock(ChatService::class.java)
    private val similarity = SimilarityCalculator(Clock.fixed(Instant.parse("2026-06-28T00:00:00Z"), ZoneOffset.UTC))
    private val published = mutableListOf<Any>()
    private val service =
        MatchingService(
            requests, profileService, feedActivity, activity, chatService, similarity,
            ApplicationEventPublisher { published.add(it) }, 0.5, Clock.systemUTC(),
        )

    private val alice = UUID.randomUUID()
    private val bob = UUID.randomUUID()

    private fun stub(userId: UUID, birthdate: LocalDate, interests: Set<Long>) {
        given(profileService.find(userId))
            .willReturn(UserProfile(userId, "F", "L", birthdate, ZoneOffset.UTC, interests.map { Interest(it, "i$it") }))
        given(feedActivity.likedPostIds(userId)).willReturn(emptySet())
        given(feedActivity.commentedPostIds(userId)).willReturn(emptySet())
        given(activity.histogram(userId)).willReturn(IntArray(24))
    }

    @Test
    fun `matches a similar queued candidate into an anonymous chat`() {
        stub(alice, LocalDate.of(2000, 1, 1), setOf(1, 2))
        stub(bob, LocalDate.of(2000, 1, 1), setOf(1, 2))
        requests.add(bob, Instant.EPOCH)
        given(chatService.createAnonymousChat(alice, bob))
            .willReturn(Chat(7, alice, bob, Instant.EPOCH, anonymous = true))

        val result = service.requestMatch(alice)

        assertThat(result).isEqualTo(MatchResult.Matched(7))
        assertThat(requests.queue).isEmpty()
        assertThat(published).containsExactly(MatchFound(alice, bob, 7))
    }

    @Test
    fun `queues the user when no candidate is similar enough`() {
        stub(alice, LocalDate.of(2000, 1, 1), setOf(1, 2))
        stub(bob, LocalDate.of(1950, 1, 1), setOf(3, 4))
        requests.add(bob, Instant.EPOCH)

        val result = service.requestMatch(alice)

        assertThat(result).isEqualTo(MatchResult.Queued)
        assertThat(requests.queue).contains(alice)
        assertThat(published).isEmpty()
    }

    @Test
    fun `requires a profile to be matched`() {
        given(profileService.find(alice)).willReturn(null)

        assertThatThrownBy { service.requestMatch(alice) }.isInstanceOf(ProfileRequiredException::class.java)
    }

    @Test
    fun `leaving the queue removes the user`() {
        requests.add(alice, Instant.EPOCH)

        service.leaveQueue(alice)

        assertThat(requests.queue).isEmpty()
    }

    private class FakeMatchRequestRepository : MatchRequestRepository {
        val queue = linkedSetOf<UUID>()

        override fun add(userId: UUID, createdAt: Instant) {
            queue.add(userId)
        }

        override fun remove(userId: UUID): Boolean = queue.remove(userId)

        override fun others(excluding: UUID): List<UUID> = queue.filter { it != excluding }
    }
}
