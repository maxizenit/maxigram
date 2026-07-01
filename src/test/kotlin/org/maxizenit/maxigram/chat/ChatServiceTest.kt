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
    private val publishedEvents = mutableListOf<Any>()
    private val service =
        ChatService(chats, { publishedEvents.add(it) }, Clock.fixed(Instant.EPOCH, ZoneOffset.UTC))

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

    @Test
    fun `the first participant's consent sets only their own flag`() {
        val chat = service.createAnonymousChat(alice, bob)

        val updated = service.agreeToDeAnonymization(chat.id, alice)

        assertThat(updated.firstAgreed).isTrue()
        assertThat(updated.secondAgreed).isFalse()
        assertThat(updated.anonymous).isTrue()
    }

    @Test
    fun `the second participant's consent sets the second flag (v1 set it to false)`() {
        val chat = service.createAnonymousChat(alice, bob)

        val updated = service.agreeToDeAnonymization(chat.id, bob)

        assertThat(updated.secondAgreed).isTrue()
        assertThat(updated.firstAgreed).isFalse()
        assertThat(updated.anonymous).isTrue()
    }

    @Test
    fun `mutual consent converts the chat itself into a regular one`() {
        val chat = service.createAnonymousChat(alice, bob)

        service.agreeToDeAnonymization(chat.id, alice)
        val deAnonymized = service.agreeToDeAnonymization(chat.id, bob)

        assertThat(deAnonymized.id).isEqualTo(chat.id)
        assertThat(deAnonymized.anonymous).isFalse()
        // Converted in place: no extra chat is created.
        assertThat(chats.chats).hasSize(1)
        assertThat(chats.chats.single().anonymous).isFalse()
    }

    @Test
    fun `consent and close publish a state-changed event for live updates`() {
        val chat = service.createAnonymousChat(alice, bob)

        service.agreeToDeAnonymization(chat.id, alice)
        service.closeAnonymousChat(chat.id, bob)

        assertThat(publishedEvents).containsExactly(ChatStateChanged(chat.id), ChatStateChanged(chat.id))
    }

    @Test
    fun `cannot agree to de-anonymize a non-anonymous chat`() {
        val regular = service.openChatWith(alice, bob)

        assertThatThrownBy { service.agreeToDeAnonymization(regular.id, alice) }
            .isInstanceOf(InvalidChatException::class.java)
    }

    @Test
    fun `cannot agree once the chat is closed`() {
        val chat = service.createAnonymousChat(alice, bob)
        service.closeAnonymousChat(chat.id, alice)

        assertThatThrownBy { service.agreeToDeAnonymization(chat.id, alice) }
            .isInstanceOf(InvalidChatException::class.java)
    }

    @Test
    fun `closing twice is rejected`() {
        val chat = service.createAnonymousChat(alice, bob)
        service.closeAnonymousChat(chat.id, alice)

        assertThatThrownBy { service.closeAnonymousChat(chat.id, alice) }
            .isInstanceOf(InvalidChatException::class.java)
    }
}
