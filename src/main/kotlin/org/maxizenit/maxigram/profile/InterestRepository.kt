package org.maxizenit.maxigram.profile

import org.jooq.DSLContext
import org.maxizenit.maxigram.jooq.Tables.INTEREST
import org.springframework.stereotype.Repository

interface InterestRepository {
    fun findAll(): List<Interest>
    fun findByIds(ids: Collection<Long>): List<Interest>
}

@Repository
class JooqInterestRepository(private val dsl: DSLContext) : InterestRepository {

    override fun findAll(): List<Interest> =
        dsl.select(INTEREST.ID, INTEREST.NAME)
            .from(INTEREST)
            .orderBy(INTEREST.ID)
            .fetch { Interest(it.get(INTEREST.ID), it.get(INTEREST.NAME)) }

    override fun findByIds(ids: Collection<Long>): List<Interest> {
        if (ids.isEmpty()) return emptyList()
        return dsl.select(INTEREST.ID, INTEREST.NAME)
            .from(INTEREST)
            .where(INTEREST.ID.`in`(ids))
            .orderBy(INTEREST.ID)
            .fetch { Interest(it.get(INTEREST.ID), it.get(INTEREST.NAME)) }
    }
}
