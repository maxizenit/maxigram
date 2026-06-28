package org.maxizenit.maxigram.matching

import java.time.LocalDate
import java.util.UUID

/** Everything the similarity calculator needs about one candidate. */
data class MatchingProfile(
    val userId: UUID,
    val birthdate: LocalDate,
    val interestIds: Set<Long>,
    val likedPostIds: Set<Long>,
    val commentedPostIds: Set<Long>,
    val activityHistogram: IntArray,
)

sealed interface MatchResult {
    data class Matched(val chatId: Long) : MatchResult

    data object Queued : MatchResult
}

/** Published when two queued users are matched into an anonymous chat. */
data class MatchFound(val firstUserId: UUID, val secondUserId: UUID, val chatId: Long)

class ProfileRequiredException : RuntimeException("A profile is required to be matched")
