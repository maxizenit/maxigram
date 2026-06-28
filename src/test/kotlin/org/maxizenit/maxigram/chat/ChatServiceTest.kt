package org.maxizenit.maxigram.chat

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID

class ChatServiceTest {

    private val chats = FakeChatRepository()
    private val service = ChatService(chats, Clock.fixed(Instant.EPOCH, ZoneOffset.UTC))

    private val alice = UUID.randomUUID()
    private val bob = UUID.randomUUID()

    @Test
    fun `opening a chat creates it once and reuses it regardless of order`() {
        val created = service.openChatWith(alice, bob)
        val reused = service.openChatWith(bob, alice)

        assertThat(reused.id).isEqualTo(created.id)
        assertThat(chats.chats).hasSize(1)
    }

    @Test
    fun `cannot open a chat with yourself`() {
        assertThatThrownBy { service.openChatWith(alice, alice) }
            .isInstanceOf(InvalidChatException::class.java)
    }

    @Test
    fun `requireParticipant returns the chat for a participant`() {
        val chat = service.openChatWith(alice, bob)

        assertThat(service.requireParticipant(chat.id, bob).id).isEqualTo(chat.id)
    }

    @Test
    fun `requireParticipant rejects a non-participant`() {
        val chat = service.openChatWith(alice, bob)

        assertThatThrownBy { service.requireParticipant(chat.id, UUID.randomUUID()) }
            .isInstanceOf(ChatAccessDeniedException::class.java)
    }

    @Test
    fun `requireParticipant throws for a missing chat`() {
        assertThatThrownBy { service.requireParticipant(404L, alice) }
            .isInstanceOf(ChatNotFoundException::class.java)
    }
}
