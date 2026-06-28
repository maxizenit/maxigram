package org.maxizenit.maxigram.profile

import org.jooq.DSLContext
import org.maxizenit.maxigram.jooq.Tables.INTEREST
import org.maxizenit.maxigram.jooq.Tables.USER_INTEREST
import org.maxizenit.maxigram.jooq.Tables.USER_PROFILE
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID

interface ProfileRepository {
    fun upsert(userId: UUID, firstName: String, lastName: String, birthdate: LocalDate, timezone: ZoneId)
    fun findById(userId: UUID): UserProfile?
    fun timezoneOf(userId: UUID): ZoneId?
    fun replaceInterests(userId: UUID, interestIds: List<Long>)
    fun findInterestsOf(userId: UUID): List<Interest>
}

@Repository
class JooqProfileRepository(private val dsl: DSLContext) : ProfileRepository {

    override fun upsert(userId: UUID, firstName: String, lastName: String, birthdate: LocalDate, timezone: ZoneId) {
        dsl.insertInto(USER_PROFILE)
            .set(USER_PROFILE.ID, userId)
            .set(USER_PROFILE.FIRST_NAME, firstName)
            .set(USER_PROFILE.LAST_NAME, lastName)
            .set(USER_PROFILE.BIRTHDATE, birthdate)
            .set(USER_PROFILE.TIMEZONE, timezone.id)
            .onConflict(USER_PROFILE.ID)
            .doUpdate()
            .set(USER_PROFILE.FIRST_NAME, firstName)
            .set(USER_PROFILE.LAST_NAME, lastName)
            .set(USER_PROFILE.BIRTHDATE, birthdate)
            .set(USER_PROFILE.TIMEZONE, timezone.id)
            .execute()
    }

    override fun findById(userId: UUID): UserProfile? =
        dsl.select(
            USER_PROFILE.ID,
            USER_PROFILE.FIRST_NAME,
            USER_PROFILE.LAST_NAME,
            USER_PROFILE.BIRTHDATE,
            USER_PROFILE.TIMEZONE,
        )
            .from(USER_PROFILE)
            .where(USER_PROFILE.ID.eq(userId))
            .fetchOne()
            ?.let {
                UserProfile(
                    id = it.get(USER_PROFILE.ID),
                    firstName = it.get(USER_PROFILE.FIRST_NAME),
                    lastName = it.get(USER_PROFILE.LAST_NAME),
                    birthdate = it.get(USER_PROFILE.BIRTHDATE),
                    timezone = ZoneId.of(it.get(USER_PROFILE.TIMEZONE)),
                    interests = emptyList(),
                )
            }

    override fun timezoneOf(userId: UUID): ZoneId? =
        dsl.select(USER_PROFILE.TIMEZONE)
            .from(USER_PROFILE)
            .where(USER_PROFILE.ID.eq(userId))
            .fetchOne(USER_PROFILE.TIMEZONE)
            ?.let { ZoneId.of(it) }

    override fun replaceInterests(userId: UUID, interestIds: List<Long>) {
        dsl.deleteFrom(USER_INTEREST).where(USER_INTEREST.USER_ID.eq(userId)).execute()
        if (interestIds.isEmpty()) return
        var insert =
            dsl.insertInto(USER_INTEREST, USER_INTEREST.USER_ID, USER_INTEREST.INTEREST_ID)
                .values(userId, interestIds.first())
        for (interestId in interestIds.drop(1)) {
            insert = insert.values(userId, interestId)
        }
        insert.execute()
    }

    override fun findInterestsOf(userId: UUID): List<Interest> =
        dsl.select(INTEREST.ID, INTEREST.NAME)
            .from(INTEREST)
            .join(USER_INTEREST).on(USER_INTEREST.INTEREST_ID.eq(INTEREST.ID))
            .where(USER_INTEREST.USER_ID.eq(userId))
            .orderBy(INTEREST.ID)
            .fetch { Interest(it.get(INTEREST.ID), it.get(INTEREST.NAME)) }
}
