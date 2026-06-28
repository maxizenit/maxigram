package org.maxizenit.maxigram.notification

import org.junit.jupiter.api.Test
import org.maxizenit.maxigram.chat.Message
import org.maxizenit.maxigram.chat.MessageSent
import org.maxizenit.maxigram.feed.PostCommented
import org.maxizenit.maxigram.feed.PostLiked
import org.maxizenit.maxigram.profile.SubscriptionCreated
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import java.time.Instant
import java.util.UUID

class NotificationListenersTest {

    private val service = mock(NotificationService::class.java)
    private val listeners = NotificationListeners(service)

    private val author = UUID.randomUUID()
    private val actor = UUID.randomUUID()

    @Test
    fun `a new message notifies the recipient`() {
        val message = Message(1, 10, actor, "hi", Instant.EPOCH, read = false)

        listeners.onMessageSent(MessageSent(10, author, message))

        verify(service).notify(author, NotificationType.NEW_MESSAGE, actor, "Новое сообщение")
    }

    @Test
    fun `a new subscription notifies the author`() {
        listeners.onSubscriptionCreated(SubscriptionCreated(actor, author))

        verify(service).notify(author, NotificationType.NEW_SUBSCRIBER, actor, "Новый подписчик")
    }

    @Test
    fun `a like notifies the post author`() {
        listeners.onPostLiked(PostLiked(5, author, actor))

        verify(service).notify(author, NotificationType.POST_LIKED, actor, "Ваш пост оценили")
    }

    @Test
    fun `liking your own post does not notify`() {
        listeners.onPostLiked(PostLiked(5, author, author))

        verifyNoInteractions(service)
    }

    @Test
    fun `a comment notifies the post author`() {
        listeners.onPostCommented(PostCommented(5, author, actor))

        verify(service).notify(author, NotificationType.POST_COMMENTED, actor, "Новый комментарий")
    }
}
