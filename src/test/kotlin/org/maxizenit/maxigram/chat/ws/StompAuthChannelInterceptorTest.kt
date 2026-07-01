package org.maxizenit.maxigram.chat.ws

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.maxizenit.maxigram.chat.ChatAccessDeniedException
import org.maxizenit.maxigram.chat.ChatService
import org.maxizenit.maxigram.chat.FakeChatRepository
import org.mockito.BDDMockito.given
import org.mockito.Mockito.mock
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.MessagingException
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.MessageBuilder
import org.springframework.messaging.support.MessageHeaderAccessor
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import java.time.Clock
import java.time.Instant
import java.util.UUID

class StompAuthChannelInterceptorTest {

    private val jwtDecoder = mock(JwtDecoder::class.java)
    private val chats = FakeChatRepository()
    private val chatService = ChatService(chats, {}, Clock.systemUTC())
    private val interceptor = StompAuthChannelInterceptor(jwtDecoder, chatService)
    private val channel = mock(MessageChannel::class.java)

    private val alice = UUID.randomUUID()
    private val bob = UUID.randomUUID()
    private val stranger = UUID.randomUUID()
    private val chat = chats.insert(alice, bob, Instant.EPOCH)

    private fun jwtFor(userId: UUID): Jwt =
        Jwt.withTokenValue("token").header("alg", "none").subject(userId.toString()).build()

    private fun frame(command: StompCommand, configure: (StompHeaderAccessor) -> Unit): Message<ByteArray> {
        val accessor = StompHeaderAccessor.create(command)
        accessor.setLeaveMutable(true)
        configure(accessor)
        return MessageBuilder.createMessage(ByteArray(0), accessor.messageHeaders)
    }

    @Test
    fun `connect with a valid token authenticates the session`() {
        given(jwtDecoder.decode("good")).willReturn(jwtFor(bob))

        val message = frame(StompCommand.CONNECT) { it.setNativeHeader("Authorization", "Bearer good") }
        interceptor.preSend(message, channel)

        val user = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor::class.java)!!.user
        assertThat(user).isInstanceOf(JwtAuthenticationToken::class.java)
    }

    @Test
    fun `connect without a token is rejected`() {
        val message = frame(StompCommand.CONNECT) {}

        assertThatThrownBy { interceptor.preSend(message, channel) }
            .isInstanceOf(MessagingException::class.java)
    }

    @Test
    fun `a participant may subscribe to their chat`() {
        val message = frame(StompCommand.SUBSCRIBE) {
            it.destination = "/topic/chats/${chat.id}"
            it.user = JwtAuthenticationToken(jwtFor(bob))
        }

        assertThatCode { interceptor.preSend(message, channel) }.doesNotThrowAnyException()
    }

    @Test
    fun `a non-participant may not subscribe to a chat`() {
        val message = frame(StompCommand.SUBSCRIBE) {
            it.destination = "/topic/chats/${chat.id}"
            it.user = JwtAuthenticationToken(jwtFor(stranger))
        }

        assertThatThrownBy { interceptor.preSend(message, channel) }
            .isInstanceOf(ChatAccessDeniedException::class.java)
    }

    @Test
    fun `the state sub-topic is guarded the same way`() {
        val allowed = frame(StompCommand.SUBSCRIBE) {
            it.destination = "/topic/chats/${chat.id}/state"
            it.user = JwtAuthenticationToken(jwtFor(alice))
        }
        assertThatCode { interceptor.preSend(allowed, channel) }.doesNotThrowAnyException()

        val denied = frame(StompCommand.SUBSCRIBE) {
            it.destination = "/topic/chats/${chat.id}/state"
            it.user = JwtAuthenticationToken(jwtFor(stranger))
        }
        assertThatThrownBy { interceptor.preSend(denied, channel) }
            .isInstanceOf(ChatAccessDeniedException::class.java)
    }
}
