package org.maxizenit.maxigram.profile

import org.jooq.DSLContext
import org.maxizenit.maxigram.jooq.Tables.SUBSCRIPTION
import org.springframework.stereotype.Repository
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID

interface SubscriptionRepository {
    /** Returns true if a new subscription was created, false if it already existed. */
    fun insertIfAbsent(subscriberId: UUID, authorId: UUID, createdAt: Instant): Boolean
    fun delete(subscriberId: UUID, authorId: UUID)
    fun findAuthorIds(subscriberId: UUID): List<UUID>
}

@Repository
class JooqSubscriptionRepository(private val dsl: DSLContext) : SubscriptionRepository {

    override fun insertIfAbsent(subscriberId: UUID, authorId: UUID, createdAt: Instant): Boolean =
        dsl.insertInto(SUBSCRIPTION)
            .set(SUBSCRIPTION.SUBSCRIBER_ID, subscriberId)
            .set(SUBSCRIPTION.AUTHOR_ID, authorId)
            .set(SUBSCRIPTION.CREATED_AT, createdAt.atOffset(ZoneOffset.UTC))
            .onConflictDoNothing()
            .execute() > 0

    override fun delete(subscriberId: UUID, authorId: UUID) {
        dsl.deleteFrom(SUBSCRIPTION)
            .where(SUBSCRIPTION.SUBSCRIBER_ID.eq(subscriberId).and(SUBSCRIPTION.AUTHOR_ID.eq(authorId)))
            .execute()
    }

    override fun findAuthorIds(subscriberId: UUID): List<UUID> =
        dsl.select(SUBSCRIPTION.AUTHOR_ID)
            .from(SUBSCRIPTION)
            .where(SUBSCRIPTION.SUBSCRIBER_ID.eq(subscriberId))
            .orderBy(SUBSCRIPTION.CREATED_AT)
            .fetch(SUBSCRIPTION.AUTHOR_ID)
}
