package org.maxizenit.maxigram.notification

import org.jooq.DSLContext
import org.maxizenit.maxigram.jooq.Tables.NOTIFICATION
import org.springframework.stereotype.Repository
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID

interface NotificationRepository {
    fun insert(recipientId: UUID, type: NotificationType, actorId: UUID?, text: String, createdAt: Instant): Notification
    fun findByRecipient(recipientId: UUID): List<Notification>
    fun markRead(id: Long, recipientId: UUID): Boolean
}

@Repository
class JooqNotificationRepository(private val dsl: DSLContext) : NotificationRepository {

    override fun insert(
        recipientId: UUID,
        type: NotificationType,
        actorId: UUID?,
        text: String,
        createdAt: Instant,
    ): Notification {
        val id =
            dsl.insertInto(NOTIFICATION)
                .set(NOTIFICATION.RECIPIENT_ID, recipientId)
                .set(NOTIFICATION.TYPE, type.name)
                .set(NOTIFICATION.ACTOR_ID, actorId)
                .set(NOTIFICATION.TEXT, text)
                .set(NOTIFICATION.READ, false)
                .set(NOTIFICATION.CREATED_AT, createdAt.atOffset(ZoneOffset.UTC))
                .returningResult(NOTIFICATION.ID)
                .fetchOne()!!
                .value1()!!
        return Notification(id, recipientId, type, actorId, text, read = false, createdAt = createdAt)
    }

    override fun findByRecipient(recipientId: UUID): List<Notification> =
        dsl.select(
            NOTIFICATION.ID,
            NOTIFICATION.RECIPIENT_ID,
            NOTIFICATION.TYPE,
            NOTIFICATION.ACTOR_ID,
            NOTIFICATION.TEXT,
            NOTIFICATION.READ,
            NOTIFICATION.CREATED_AT,
        )
            .from(NOTIFICATION)
            .where(NOTIFICATION.RECIPIENT_ID.eq(recipientId))
            .orderBy(NOTIFICATION.CREATED_AT.desc(), NOTIFICATION.ID.desc())
            .fetch {
                Notification(
                    id = it[NOTIFICATION.ID],
                    recipientId = it[NOTIFICATION.RECIPIENT_ID],
                    type = NotificationType.valueOf(it[NOTIFICATION.TYPE]),
                    actorId = it[NOTIFICATION.ACTOR_ID],
                    text = it[NOTIFICATION.TEXT],
                    read = it[NOTIFICATION.READ],
                    createdAt = it[NOTIFICATION.CREATED_AT].toInstant(),
                )
            }

    override fun markRead(id: Long, recipientId: UUID): Boolean =
        dsl.update(NOTIFICATION)
            .set(NOTIFICATION.READ, true)
            .where(NOTIFICATION.ID.eq(id).and(NOTIFICATION.RECIPIENT_ID.eq(recipientId)))
            .execute() > 0
}
