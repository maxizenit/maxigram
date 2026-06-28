package org.maxizenit.maxigram.identity

import org.springframework.stereotype.Service
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.util.UUID

/** Issues and consumes single-use, expiring tokens (email verification, password reset). */
@Service
class OneTimeTokenService(
    private val tokens: OneTimeTokenRepository,
    private val clock: Clock = Clock.systemUTC(),
) {

    fun issue(userId: UUID, purpose: TokenPurpose, ttl: Duration): UUID {
        val token = UUID.randomUUID()
        tokens.insert(token, userId, purpose, Instant.now(clock).plus(ttl))
        return token
    }

    /** Validates the token for [purpose] and marks it consumed, or throws [InvalidTokenException]. */
    fun consume(token: UUID, purpose: TokenPurpose): OneTimeToken {
        val record = tokens.find(token) ?: throw InvalidTokenException("Token not found")
        if (record.purpose != purpose) throw InvalidTokenException("Wrong token purpose")
        if (record.consumedAt != null) throw InvalidTokenException("Token already used")
        if (!record.expiresAt.isAfter(Instant.now(clock))) throw InvalidTokenException("Token expired")
        tokens.markConsumed(token, Instant.now(clock))
        return record
    }
}
