package org.maxizenit.maxigram.chat.ws

import org.maxizenit.maxigram.chat.ChatService
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.MessagingException
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.messaging.support.MessageHeaderAccessor
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Component
import java.util.UUID

/**
 * Authenticates the STOMP session from the `Authorization: Bearer` header on CONNECT, and
 * authorizes SUBSCRIBE so a user can only listen to chats they participate in.
 */
@Component
class StompAuthChannelInterceptor(
    private val jwtDecoder: JwtDecoder,
    private val chatService: ChatService,
) : ChannelInterceptor {

    override fun preSend(message: Message<*>, channel: MessageChannel): Message<*> {
        val accessor =
            MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor::class.java) ?: return message
        when (accessor.command) {
            StompCommand.CONNECT -> accessor.user = authenticate(accessor)
            StompCommand.SUBSCRIBE -> authorizeSubscription(accessor)
            else -> Unit
        }
        return message
    }

    private fun authenticate(accessor: StompHeaderAccessor): JwtAuthenticationToken {
        val header = accessor.getFirstNativeHeader("Authorization")
            ?: throw MessagingException("Missing Authorization header")
        if (!header.startsWith("Bearer ")) throw MessagingException("Invalid Authorization header")
        val jwt =
            try {
                jwtDecoder.decode(header.removePrefix("Bearer ").trim())
            } catch (e: Exception) {
                throw MessagingException("Invalid token", e)
            }
        return JwtAuthenticationToken(jwt)
    }

    private fun authorizeSubscription(accessor: StompHeaderAccessor) {
        val chatId = CHAT_TOPIC.matchEntire(accessor.destination ?: return)?.groupValues?.get(1)?.toLong() ?: return
        val principal = accessor.user as? JwtAuthenticationToken ?: throw MessagingException("Not authenticated")
        chatService.requireParticipant(chatId, UUID.fromString(principal.token.subject))
    }

    private companion object {
        /** Matches both the message topic and its /state sub-topic. */
        val CHAT_TOPIC = Regex("/topic/chats/(\\d+)(?:/state)?")
    }
}
