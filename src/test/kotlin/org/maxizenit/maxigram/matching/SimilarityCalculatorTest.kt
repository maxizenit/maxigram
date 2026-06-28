package org.maxizenit.maxigram.matching

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.within
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.UUID

class SimilarityCalculatorTest {

    private val calc = SimilarityCalculator(Clock.fixed(Instant.parse("2026-06-28T00:00:00Z"), ZoneOffset.UTC))

    @Test
    fun `same age scores 1, a large gap scores 0`() {
        assertThat(calc.ageSimilarity(LocalDate.of(2000, 1, 1), LocalDate.of(2000, 1, 1))).isEqualTo(1.0)
        assertThat(calc.ageSimilarity(LocalDate.of(2000, 1, 1), LocalDate.of(1950, 1, 1))).isEqualTo(0.0)
    }

    @Test
    fun `interest similarity is symmetric and never blows up`() {
        assertThat(calc.interestSimilarity(setOf(1, 2), setOf(1, 2))).isEqualTo(1.0)
        assertThat(calc.interestSimilarity(setOf(1, 2), setOf(3, 4))).isEqualTo(0.0)
        assertThat(calc.interestSimilarity(setOf(1, 2), setOf(2, 3))).isEqualTo(0.5)
        // v1 produced Infinity here (one side empty); must be finite.
        assertThat(calc.interestSimilarity(setOf(1, 2), emptySet())).isEqualTo(0.0)
        assertThat(calc.interestSimilarity(emptySet(), emptySet())).isNull()
    }

    @Test
    fun `time similarity is cosine of histograms, null without activity`() {
        val morning = IntArray(24).also { it[9] = 5; it[10] = 3 }
        val evening = IntArray(24).also { it[20] = 5; it[21] = 3 }

        assertThat(calc.timeSimilarity(morning, morning)).isCloseTo(1.0, within(1e-9))
        assertThat(calc.timeSimilarity(morning, evening)).isEqualTo(0.0)
        assertThat(calc.timeSimilarity(morning, IntArray(24))).isNull()
    }

    @Test
    fun `identical candidates score 1, factors without data are dropped`() {
        val a = profile(LocalDate.of(2000, 1, 1), setOf(1, 2))
        val b = profile(LocalDate.of(2000, 1, 1), setOf(1, 2))

        // Only age + interests have data here (feed/time empty) -> still a perfect match.
        assertThat(calc.similarity(a, b)).isCloseTo(1.0, within(1e-9))
    }

    private fun profile(birthdate: LocalDate, interests: Set<Long>) =
        MatchingProfile(UUID.randomUUID(), birthdate, interests, emptySet(), emptySet(), IntArray(24))
}
