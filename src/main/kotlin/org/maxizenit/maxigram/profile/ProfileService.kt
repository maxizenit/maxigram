package org.maxizenit.maxigram.profile

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID

@Service
class ProfileService(
    private val profiles: ProfileRepository,
    private val interests: InterestRepository,
    private val clock: Clock = Clock.systemUTC(),
) {

    @Transactional
    fun save(
        userId: UUID,
        firstName: String,
        lastName: String,
        birthdate: LocalDate,
        timezone: String,
        interestIds: List<Long>,
    ): UserProfile {
        val trimmedFirstName = firstName.trim()
        val trimmedLastName = lastName.trim()
        if (trimmedFirstName.isEmpty() || trimmedLastName.isEmpty()) {
            throw InvalidProfileException("First and last name must not be blank")
        }
        if (!birthdate.isBefore(LocalDate.now(clock))) {
            throw InvalidProfileException("Birthdate must be in the past")
        }
        val zone =
            try {
                ZoneId.of(timezone)
            } catch (e: Exception) {
                throw InvalidProfileException("Invalid timezone: $timezone")
            }
        val distinctIds = interestIds.distinct()
        val known = interests.findByIds(distinctIds)
        if (known.size != distinctIds.size) {
            throw InvalidProfileException("Unknown interest id")
        }

        profiles.upsert(userId, trimmedFirstName, trimmedLastName, birthdate, zone)
        profiles.replaceInterests(userId, distinctIds)
        return UserProfile(userId, trimmedFirstName, trimmedLastName, birthdate, zone, known)
    }

    @Transactional(readOnly = true)
    fun find(userId: UUID): UserProfile? {
        val profile = profiles.findById(userId) ?: return null
        return profile.copy(interests = profiles.findInterestsOf(userId))
    }

    fun timezoneOf(userId: UUID): ZoneId? = profiles.timezoneOf(userId)

    fun listInterests(): List<Interest> = interests.findAll()

    /** Name search for the people screen; interests are not loaded for list views. */
    fun search(query: String): List<UserProfile> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return emptyList()
        return profiles.searchByName(trimmed, limit = 20)
    }

    fun findAllByIds(ids: Collection<UUID>): List<UserProfile> = profiles.findByIds(ids)
}
