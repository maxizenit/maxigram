package org.maxizenit.maxigram.matching

import org.springframework.stereotype.Component
import java.time.Clock
import java.time.LocalDate
import java.time.Period
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sqrt

/**
 * Computes a 0..1 similarity between two candidates as a weighted average of available factors.
 * Factors with no data return null and are dropped (weights renormalized), so cold-start users
 * are still matched on whatever signal exists. Fixes the v1 bugs (#5 Infinity, #11 age).
 */
@Component
class SimilarityCalculator(private val clock: Clock = Clock.systemUTC()) {

    fun similarity(a: MatchingProfile, b: MatchingProfile): Double {
        val factors =
            listOfNotNull(
                AGE_WEIGHT to ageSimilarity(a.birthdate, b.birthdate),
                interestSimilarity(a.interestIds, b.interestIds)?.let { INTEREST_WEIGHT to it },
                feedSimilarity(a, b)?.let { FEED_WEIGHT to it },
                timeSimilarity(a.activityHistogram, b.activityHistogram)?.let { TIME_WEIGHT to it },
            )
        val totalWeight = factors.sumOf { it.first }
        if (totalWeight == 0.0) return 0.0
        return factors.sumOf { it.first * it.second } / totalWeight
    }

    fun ageSimilarity(a: LocalDate, b: LocalDate): Double {
        val today = LocalDate.now(clock)
        val ageA = Period.between(a, today).years
        val ageB = Period.between(b, today).years
        return max(1.0 - abs(ageA - ageB) * 0.05, 0.0)
    }

    /** Symmetric overlap; null when neither side has any interests. */
    fun interestSimilarity(a: Set<Long>, b: Set<Long>): Double? = setSimilarity(a, b)

    private fun feedSimilarity(a: MatchingProfile, b: MatchingProfile): Double? {
        val parts =
            listOfNotNull(
                setSimilarity(a.likedPostIds, b.likedPostIds),
                setSimilarity(a.commentedPostIds, b.commentedPostIds),
            )
        return if (parts.isEmpty()) null else parts.average()
    }

    private fun setSimilarity(a: Set<Long>, b: Set<Long>): Double? {
        if (a.isEmpty() && b.isEmpty()) return null
        val common = a.intersect(b).size.toDouble()
        val fromA = if (a.isEmpty()) 0.0 else common / a.size
        val fromB = if (b.isEmpty()) 0.0 else common / b.size
        return (fromA + fromB) / 2
    }

    /** Cosine similarity of two activity histograms; null when either has no recorded activity. */
    fun timeSimilarity(a: IntArray, b: IntArray): Double? {
        val dot = a.indices.sumOf { a[it].toDouble() * b[it] }
        val normA = sqrt(a.sumOf { it.toDouble() * it })
        val normB = sqrt(b.sumOf { it.toDouble() * it })
        if (normA == 0.0 || normB == 0.0) return null
        return dot / (normA * normB)
    }

    private companion object {
        const val AGE_WEIGHT = 0.3
        const val INTEREST_WEIGHT = 0.2
        const val FEED_WEIGHT = 0.2
        const val TIME_WEIGHT = 0.3
    }
}
