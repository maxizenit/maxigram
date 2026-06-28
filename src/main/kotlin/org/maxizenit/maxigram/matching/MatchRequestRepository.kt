package org.maxizenit.maxigram.matching

import org.jooq.DSLContext
import org.maxizenit.maxigram.jooq.Tables.MATCH_REQUEST
import org.springframework.stereotype.Repository
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID

interface MatchRequestRepository {
    fun add(userId: UUID, createdAt: Instant)
    fun remove(userId: UUID): Boolean
    fun others(excluding: UUID): List<UUID>
}

@Repository
class JooqMatchRequestRepository(private val dsl: DSLContext) : MatchRequestRepository {

    override fun add(userId: UUID, createdAt: Instant) {
        dsl.insertInto(MATCH_REQUEST)
            .set(MATCH_REQUEST.USER_ID, userId)
            .set(MATCH_REQUEST.CREATED_AT, createdAt.atOffset(ZoneOffset.UTC))
            .onConflictDoNothing()
            .execute()
    }

    override fun remove(userId: UUID): Boolean =
        dsl.deleteFrom(MATCH_REQUEST).where(MATCH_REQUEST.USER_ID.eq(userId)).execute() > 0

    override fun others(excluding: UUID): List<UUID> =
        dsl.select(MATCH_REQUEST.USER_ID)
            .from(MATCH_REQUEST)
            .where(MATCH_REQUEST.USER_ID.ne(excluding))
            .orderBy(MATCH_REQUEST.CREATED_AT)
            .fetch(MATCH_REQUEST.USER_ID)
}
