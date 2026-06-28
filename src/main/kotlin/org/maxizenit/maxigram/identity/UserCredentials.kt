package org.maxizenit.maxigram.identity

import java.util.UUID

/** Authentication-time view of an account, including the password hash. */
data class UserCredentials(
    val id: UUID,
    val email: String,
    val passwordHash: String,
    val emailVerified: Boolean,
)
