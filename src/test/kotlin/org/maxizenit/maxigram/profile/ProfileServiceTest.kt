package org.maxizenit.maxigram.profile

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.UUID

class ProfileServiceTest {

    private val catalogue = mapOf(1L to Interest(1, "Футбол"), 2L to Interest(2, "Музыка"))
    private val profiles = FakeProfileRepository(catalogue)
    private val interests = FakeInterestRepository(catalogue.values.toList())
    private val clock = Clock.fixed(Instant.parse("2026-06-28T00:00:00Z"), ZoneOffset.UTC)
    private val service = ProfileService(profiles, interests, clock)

    private val userId = UUID.randomUUID()

    @Test
    fun `saves a valid profile with interests and timezone`() {
        val profile = service.save(userId, "  Ann ", "Lee ", LocalDate.of(2000, 1, 1), "Europe/Moscow", listOf(1L))

        assertThat(profile.firstName).isEqualTo("Ann")
        assertThat(profile.timezone).isEqualTo(ZoneId.of("Europe/Moscow"))
        assertThat(profile.interests).extracting<String> { it.name }.containsExactly("Футбол")
    }

    @Test
    fun `rejects blank names`() {
        assertThatThrownBy { service.save(userId, "  ", "Lee", LocalDate.of(2000, 1, 1), "UTC", emptyList()) }
            .isInstanceOf(InvalidProfileException::class.java)
    }

    @Test
    fun `rejects a birthdate that is not in the past`() {
        assertThatThrownBy { service.save(userId, "Ann", "Lee", LocalDate.of(2030, 1, 1), "UTC", emptyList()) }
            .isInstanceOf(InvalidProfileException::class.java)
    }

    @Test
    fun `rejects an invalid timezone`() {
        assertThatThrownBy { service.save(userId, "Ann", "Lee", LocalDate.of(2000, 1, 1), "Mars/Phobos", emptyList()) }
            .isInstanceOf(InvalidProfileException::class.java)
    }

    @Test
    fun `rejects unknown interest ids`() {
        assertThatThrownBy { service.save(userId, "Ann", "Lee", LocalDate.of(2000, 1, 1), "UTC", listOf(99L)) }
            .isInstanceOf(InvalidProfileException::class.java)
    }

    @Test
    fun `find returns the saved profile with its interests`() {
        service.save(userId, "Ann", "Lee", LocalDate.of(2000, 1, 1), "UTC", listOf(1L, 2L))

        val found = service.find(userId)!!
        assertThat(found.interests).extracting<String> { it.name }.containsExactly("Футбол", "Музыка")
    }

    private class FakeProfileRepository(private val catalogue: Map<Long, Interest>) : ProfileRepository {
        private val base = mutableMapOf<UUID, UserProfile>()
        private val links = mutableMapOf<UUID, List<Long>>()

        override fun upsert(userId: UUID, firstName: String, lastName: String, birthdate: LocalDate, timezone: ZoneId) {
            base[userId] = UserProfile(userId, firstName, lastName, birthdate, timezone, emptyList())
        }

        override fun findById(userId: UUID): UserProfile? = base[userId]

        override fun timezoneOf(userId: UUID): ZoneId? = base[userId]?.timezone

        override fun replaceInterests(userId: UUID, interestIds: List<Long>) {
            links[userId] = interestIds
        }

        override fun findInterestsOf(userId: UUID): List<Interest> =
            (links[userId] ?: emptyList()).mapNotNull { catalogue[it] }
    }

    private class FakeInterestRepository(private val all: List<Interest>) : InterestRepository {
        override fun findAll(): List<Interest> = all
        override fun findByIds(ids: Collection<Long>): List<Interest> = all.filter { it.id in ids }
    }
}
