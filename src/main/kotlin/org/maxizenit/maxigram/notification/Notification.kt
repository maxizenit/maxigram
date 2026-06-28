package org.maxizenit.maxigram.notification

import java.time.Instant
import java.util.UUID

enum class NotificationType {
    NEW_MESSAGE,
    NEW_SUBSCRIBER,
    POST_LIKED,
    POST_COMMENTED,
}

data class Notification(
    val id: Long,
    val recipientId: UUID,
    val type: NotificationType,
    val actorId: UUID?,
    val text: String,
    val read: Boolean,
    val createdAt: Instant,
)

class NotificationNotFoundException(id: Long) : RuntimeException("No notification with id $id")
