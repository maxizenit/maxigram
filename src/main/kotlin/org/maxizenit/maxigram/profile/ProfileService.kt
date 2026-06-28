package org.maxizenit.maxigram.profile

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.LocalDate
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
        val distinctIds = interestIds.distinct()
        val known = interests.findByIds(distinctIds)
        if (known.size != distinctIds.size) {
            throw InvalidProfileException("Unknown interest id")
        }

        profiles.upsert(userId, trimmedFirstName, trimmedLastName, birthdate)
        profiles.replaceInterests(userId, distinctIds)
        return UserProfile(userId, trimmedFirstName, trimmedLastName, birthdate, known)
    }

    @Transactional(readOnly = true)
    fun find(userId: UUID): UserProfile? {
        val profile = profiles.findById(userId) ?: return null
        return profile.copy(interests = profiles.findInterestsOf(userId))
    }

    fun listInterests(): List<Interest> = interests.findAll()
}
