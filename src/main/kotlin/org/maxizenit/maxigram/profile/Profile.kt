package org.maxizenit.maxigram.profile

import java.time.LocalDate
import java.util.UUID

data class Interest(val id: Long, val name: String)

data class UserProfile(
    val id: UUID,
    val firstName: String,
    val lastName: String,
    val birthdate: LocalDate,
    val interests: List<Interest>,
)

class InvalidProfileException(message: String) : RuntimeException(message)

class ProfileNotFoundException(userId: UUID) : RuntimeException("No profile for user $userId")
