package org.maxizenit.maxigram.chat.ws

import org.maxizenit.maxigram.chat.MessageSent
import org.maxizenit.maxigram.chat.web.toBroadcastResponse
import org.springframework.context.event.EventListener
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Component

/** Pushes newly sent messages to the chat's STOMP topic for real-time delivery. */
@Component
class RealtimeMessageBroadcaster(private val messagingTemplate: SimpMessagingTemplate) {

    @EventListener
    fun onMessageSent(event: MessageSent) {
        messagingTemplate.convertAndSend(
            "/topic/chats/${event.chatId}",
            event.message.toBroadcastResponse(event.anonymous),
        )
    }
}
