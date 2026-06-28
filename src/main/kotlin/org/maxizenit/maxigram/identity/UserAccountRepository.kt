package org.maxizenit.maxigram.identity

import java.time.Instant
import java.util.UUID

interface UserAccountRepository {

    fun existsByEmail(email: String): Boolean

    fun insert(id: UUID, email: String, passwordHash: String, createdAt: Instant): AppUser

    fun findByEmail(email: String): AppUser?
}
