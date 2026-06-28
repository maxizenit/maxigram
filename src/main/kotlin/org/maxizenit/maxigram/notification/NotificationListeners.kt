package org.maxizenit.maxigram.notification

import org.maxizenit.maxigram.chat.MessageSent
import org.maxizenit.maxigram.feed.PostCommented
import org.maxizenit.maxigram.feed.PostLiked
import org.maxizenit.maxigram.profile.SubscriptionCreated
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

/** Turns domain events from other modules into notifications. */
@Component
class NotificationListeners(private val notifications: NotificationService) {

    @EventListener
    fun onMessageSent(event: MessageSent) {
        // Keep the sender hidden in anonymous chats.
        val actor = if (event.anonymous) null else event.message.senderId
        notifications.notify(event.recipientId, NotificationType.NEW_MESSAGE, actor, "Новое сообщение")
    }

    @EventListener
    fun onSubscriptionCreated(event: SubscriptionCreated) {
        notifications.notify(event.authorId, NotificationType.NEW_SUBSCRIBER, event.subscriberId, "Новый подписчик")
    }

    @EventListener
    fun onPostLiked(event: PostLiked) {
        if (event.likerId != event.authorId) {
            notifications.notify(event.authorId, NotificationType.POST_LIKED, event.likerId, "Ваш пост оценили")
        }
    }

    @EventListener
    fun onPostCommented(event: PostCommented) {
        if (event.commenterId != event.authorId) {
            notifications.notify(event.authorId, NotificationType.POST_COMMENTED, event.commenterId, "Новый комментарий")
        }
    }
}
