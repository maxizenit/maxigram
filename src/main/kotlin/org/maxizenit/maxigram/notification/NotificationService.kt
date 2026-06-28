package org.maxizenit.maxigram.notification

import org.springframework.stereotype.Service
import java.time.Clock
import java.time.Instant
import java.util.UUID

@Service
class NotificationService(
    private val notifications: NotificationRepository,
    private val delivery: NotificationDelivery,
    private val clock: Clock = Clock.systemUTC(),
) {

    fun notify(recipientId: UUID, type: NotificationType, actorId: UUID?, text: String): Notification {
        val notification = notifications.insert(recipientId, type, actorId, text, Instant.now(clock))
        delivery.push(notification)
        return notification
    }

    fun listFor(recipientId: UUID): List<Notification> = notifications.findByRecipient(recipientId)

    fun markRead(id: Long, recipientId: UUID) {
        if (!notifications.markRead(id, recipientId)) throw NotificationNotFoundException(id)
    }
}
