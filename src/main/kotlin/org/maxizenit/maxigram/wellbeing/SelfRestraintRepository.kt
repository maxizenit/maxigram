package org.maxizenit.maxigram.wellbeing

import org.jooq.DSLContext
import org.maxizenit.maxigram.jooq.Tables.SELF_RESTRAINT
import org.springframework.stereotype.Repository
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID

interface SelfRestraintRepository {
    fun upsert(userId: UUID, startTime: Instant, endTime: Instant)
    fun findByUser(userId: UUID): SelfRestraint?
    fun delete(userId: UUID)
}

@Repository
class JooqSelfRestraintRepository(private val dsl: DSLContext) : SelfRestraintRepository {

    override fun upsert(userId: UUID, startTime: Instant, endTime: Instant) {
        dsl.insertInto(SELF_RESTRAINT)
            .set(SELF_RESTRAINT.USER_ID, userId)
            .set(SELF_RESTRAINT.START_TIME, startTime.atOffset(ZoneOffset.UTC))
            .set(SELF_RESTRAINT.END_TIME, endTime.atOffset(ZoneOffset.UTC))
            .onConflict(SELF_RESTRAINT.USER_ID)
            .doUpdate()
            .set(SELF_RESTRAINT.START_TIME, startTime.atOffset(ZoneOffset.UTC))
            .set(SELF_RESTRAINT.END_TIME, endTime.atOffset(ZoneOffset.UTC))
            .execute()
    }

    override fun findByUser(userId: UUID): SelfRestraint? =
        dsl.select(SELF_RESTRAINT.USER_ID, SELF_RESTRAINT.START_TIME, SELF_RESTRAINT.END_TIME)
            .from(SELF_RESTRAINT)
            .where(SELF_RESTRAINT.USER_ID.eq(userId))
            .fetchOne()
            ?.let {
                SelfRestraint(
                    it[SELF_RESTRAINT.USER_ID],
                    it[SELF_RESTRAINT.START_TIME].toInstant(),
                    it[SELF_RESTRAINT.END_TIME].toInstant(),
                )
            }

    override fun delete(userId: UUID) {
        dsl.deleteFrom(SELF_RESTRAINT).where(SELF_RESTRAINT.USER_ID.eq(userId)).execute()
    }
}
