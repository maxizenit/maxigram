package org.maxizenit.maxigram.identity

import java.time.Instant
import java.util.UUID

enum class TokenPurpose {
    EMAIL_VERIFICATION,
    PASSWORD_RESET,
}

data class OneTimeToken(
    val token: UUID,
    val userId: UUID,
    val purpose: TokenPurpose,
    val expiresAt: Instant,
    val consumedAt: Instant?,
)

class InvalidTokenException(message: String) : RuntimeException(message)
