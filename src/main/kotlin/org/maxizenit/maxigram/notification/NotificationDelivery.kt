package org.maxizenit.maxigram.notification

import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Component

/** Pushes a notification to the recipient's personal STOMP queue (`/user/queue/notifications`). */
@Component
class NotificationDelivery(private val messagingTemplate: SimpMessagingTemplate) {

    fun push(notification: Notification) {
        messagingTemplate.convertAndSendToUser(
            notification.recipientId.toString(),
            "/queue/notifications",
            notification,
        )
    }
}
