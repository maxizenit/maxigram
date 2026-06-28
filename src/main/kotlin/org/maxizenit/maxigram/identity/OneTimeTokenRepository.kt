package org.maxizenit.maxigram.identity

import java.time.Instant
import java.util.UUID

interface OneTimeTokenRepository {

    fun insert(token: UUID, userId: UUID, purpose: TokenPurpose, expiresAt: Instant)

    fun find(token: UUID): OneTimeToken?

    fun markConsumed(token: UUID, consumedAt: Instant)
}
