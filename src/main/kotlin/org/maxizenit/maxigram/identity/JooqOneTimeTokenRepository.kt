package org.maxizenit.maxigram.identity

import org.jooq.DSLContext
import org.maxizenit.maxigram.jooq.Tables.ONE_TIME_TOKEN
import org.springframework.stereotype.Repository
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID

@Repository
class JooqOneTimeTokenRepository(private val dsl: DSLContext) : OneTimeTokenRepository {

    override fun insert(token: UUID, userId: UUID, purpose: TokenPurpose, expiresAt: Instant) {
        dsl.insertInto(ONE_TIME_TOKEN)
            .set(ONE_TIME_TOKEN.TOKEN, token)
            .set(ONE_TIME_TOKEN.USER_ID, userId)
            .set(ONE_TIME_TOKEN.PURPOSE, purpose.name)
            .set(ONE_TIME_TOKEN.EXPIRES_AT, expiresAt.atOffset(ZoneOffset.UTC))
            .execute()
    }

    override fun find(token: UUID): OneTimeToken? {
        val record =
            dsl.selectFrom(ONE_TIME_TOKEN).where(ONE_TIME_TOKEN.TOKEN.eq(token)).fetchOne()
                ?: return null
        return OneTimeToken(
            token = record.token,
            userId = record.userId,
            purpose = TokenPurpose.valueOf(record.purpose),
            expiresAt = record.expiresAt.toInstant(),
            consumedAt = record.consumedAt?.toInstant(),
        )
    }

    override fun markConsumed(token: UUID, consumedAt: Instant) {
        dsl.update(ONE_TIME_TOKEN)
            .set(ONE_TIME_TOKEN.CONSUMED_AT, consumedAt.atOffset(ZoneOffset.UTC))
            .where(ONE_TIME_TOKEN.TOKEN.eq(token))
            .execute()
    }
}
