package org.maxizenit.maxigram.matching

import org.maxizenit.maxigram.chat.ChatService
import org.maxizenit.maxigram.feed.FeedActivityProvider
import org.maxizenit.maxigram.matching.activity.ActivityRepository
import org.maxizenit.maxigram.profile.ProfileService
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.Instant
import java.util.UUID

@Service
class MatchingService(
    private val requests: MatchRequestRepository,
    private val profileService: ProfileService,
    private val feedActivity: FeedActivityProvider,
    private val activity: ActivityRepository,
    private val chatService: ChatService,
    private val similarity: SimilarityCalculator,
    private val events: ApplicationEventPublisher,
    @Value("\${maxigram.matching.min-similarity:0.5}") private val threshold: Double,
    private val clock: Clock = Clock.systemUTC(),
) {

    /**
     * Tries to match the user against the current queue. On a match (score >= threshold) an
     * anonymous chat is created and both are removed from the queue; otherwise the user is queued.
     */
    @Transactional
    fun requestMatch(userId: UUID): MatchResult {
        val me = loadMatchingProfile(userId) ?: throw ProfileRequiredException()

        for (candidateId in requests.others(userId)) {
            val candidate = loadMatchingProfile(candidateId) ?: continue
            if (similarity.similarity(me, candidate) >= threshold && requests.remove(candidateId)) {
                requests.remove(userId)
                val chat = chatService.createAnonymousChat(userId, candidateId)
                events.publishEvent(MatchFound(userId, candidateId, chat.id))
                return MatchResult.Matched(chat.id)
            }
        }

        requests.add(userId, Instant.now(clock))
        return MatchResult.Queued
    }

    fun leaveQueue(userId: UUID) {
        requests.remove(userId)
    }

    private fun loadMatchingProfile(userId: UUID): MatchingProfile? {
        val profile = profileService.find(userId) ?: return null
        return MatchingProfile(
            userId = userId,
            birthdate = profile.birthdate,
            interestIds = profile.interests.map { it.id }.toSet(),
            likedPostIds = feedActivity.likedPostIds(userId),
            commentedPostIds = feedActivity.commentedPostIds(userId),
            activityHistogram = activity.histogram(userId),
        )
    }
}
