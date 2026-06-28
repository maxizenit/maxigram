package org.maxizenit.maxigram.chat

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.context.ApplicationEventPublisher
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID

class MessageServiceTest {

    private val chats = FakeChatRepository()
    private val messages = FakeMessageRepository()
    private val clock = Clock.fixed(Instant.EPOCH, ZoneOffset.UTC)
    private val chatService = ChatService(chats, clock)
    private val events = ApplicationEventPublisher { }
    private val service = MessageService(messages, chatService, events, clock)

    private val alice = UUID.randomUUID()
    private val bob = UUID.randomUUID()
    private val chat = chats.insert(alice, bob, Instant.EPOCH)

    @Test
    fun `a participant can send a message`() {
        val message = service.send(chat.id, alice, "  hi  ")

        assertThat(message.text).isEqualTo("hi")
        assertThat(message.read).isFalse()
    }

    @Test
    fun `a non-participant cannot send a message`() {
        assertThatThrownBy { service.send(chat.id, UUID.randomUUID(), "hi") }
            .isInstanceOf(ChatAccessDeniedException::class.java)
    }

    @Test
    fun `blank messages are rejected`() {
        assertThatThrownBy { service.send(chat.id, alice, "   ") }
            .isInstanceOf(InvalidChatException::class.java)
    }

    @Test
    fun `reading a conversation marks the other party's messages as read`() {
        service.send(chat.id, alice, "from alice")

        val asSeenByBob = service.readConversation(chat.id, bob)

        assertThat(asSeenByBob.single().read).isTrue()
    }
}
