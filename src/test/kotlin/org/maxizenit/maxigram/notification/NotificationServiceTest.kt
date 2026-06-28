package org.maxizenit.maxigram.notification

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID

class NotificationServiceTest {

    private val repository = FakeNotificationRepository()
    private val delivery = mock(NotificationDelivery::class.java)
    private val service = NotificationService(repository, delivery, Clock.fixed(Instant.EPOCH, ZoneOffset.UTC))

    private val bob = UUID.randomUUID()
    private val alice = UUID.randomUUID()

    @Test
    fun `notify persists and delivers the notification`() {
        val notification = service.notify(bob, NotificationType.NEW_SUBSCRIBER, alice, "Новый подписчик")

        assertThat(repository.items).containsExactly(notification)
        verify(delivery).push(notification)
    }

    @Test
    fun `listFor returns the recipient's notifications`() {
        service.notify(bob, NotificationType.NEW_MESSAGE, alice, "Новое сообщение")

        assertThat(service.listFor(bob)).hasSize(1)
        assertThat(service.listFor(alice)).isEmpty()
    }

    @Test
    fun `markRead marks the recipient's own notification`() {
        val notification = service.notify(bob, NotificationType.POST_LIKED, alice, "Лайк")

        service.markRead(notification.id, bob)

        assertThat(service.listFor(bob).single().read).isTrue()
    }

    @Test
    fun `markRead of another user's notification fails`() {
        val notification = service.notify(bob, NotificationType.POST_LIKED, alice, "Лайк")

        assertThatThrownBy { service.markRead(notification.id, alice) }
            .isInstanceOf(NotificationNotFoundException::class.java)
    }

    private class FakeNotificationRepository : NotificationRepository {
        val items = mutableListOf<Notification>()
        private var seq = 0L

        override fun insert(
            recipientId: UUID,
            type: NotificationType,
            actorId: UUID?,
            text: String,
            createdAt: Instant,
        ): Notification {
            val notification = Notification(++seq, recipientId, type, actorId, text, read = false, createdAt = createdAt)
            items.add(notification)
            return notification
        }

        override fun findByRecipient(recipientId: UUID): List<Notification> =
            items.filter { it.recipientId == recipientId }.sortedByDescending { it.createdAt }

        override fun markRead(id: Long, recipientId: UUID): Boolean {
            val index = items.indexOfFirst { it.id == id && it.recipientId == recipientId }
            if (index < 0) return false
            items[index] = items[index].copy(read = true)
            return true
        }
    }
}
