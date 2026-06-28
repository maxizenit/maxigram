package org.maxizenit.maxigram.identity

import java.time.Instant
import java.util.UUID

/** A registered user account. Password hashes never leave the persistence layer. */
data class AppUser(
    val id: UUID,
    val email: String,
    val emailVerified: Boolean,
    val createdAt: Instant,
)
