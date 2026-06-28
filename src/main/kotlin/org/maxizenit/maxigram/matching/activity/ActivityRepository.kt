package org.maxizenit.maxigram.matching.activity

import org.jooq.DSLContext
import org.maxizenit.maxigram.jooq.Tables.USER_ACTIVITY_HOUR
import org.springframework.stereotype.Repository
import java.util.UUID

interface ActivityRepository {
    fun incrementHour(userId: UUID, hour: Int)
    /** A 24-element array indexed by local hour (0-23) with activity counts. */
    fun histogram(userId: UUID): IntArray
}

@Repository
class JooqActivityRepository(private val dsl: DSLContext) : ActivityRepository {

    override fun incrementHour(userId: UUID, hour: Int) {
        dsl.insertInto(USER_ACTIVITY_HOUR)
            .set(USER_ACTIVITY_HOUR.USER_ID, userId)
            .set(USER_ACTIVITY_HOUR.HOUR, hour.toShort())
            .set(USER_ACTIVITY_HOUR.COUNT, 1)
            .onConflict(USER_ACTIVITY_HOUR.USER_ID, USER_ACTIVITY_HOUR.HOUR)
            .doUpdate()
            .set(USER_ACTIVITY_HOUR.COUNT, USER_ACTIVITY_HOUR.COUNT.plus(1))
            .execute()
    }

    override fun histogram(userId: UUID): IntArray {
        val histogram = IntArray(24)
        dsl.select(USER_ACTIVITY_HOUR.HOUR, USER_ACTIVITY_HOUR.COUNT)
            .from(USER_ACTIVITY_HOUR)
            .where(USER_ACTIVITY_HOUR.USER_ID.eq(userId))
            .fetch()
            .forEach { histogram[it[USER_ACTIVITY_HOUR.HOUR].toInt()] = it[USER_ACTIVITY_HOUR.COUNT] }
        return histogram
    }
}
