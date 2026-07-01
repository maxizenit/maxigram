package org.maxizenit.maxigram.chat.ws

import org.maxizenit.maxigram.chat.ChatStateChanged
import org.maxizenit.maxigram.chat.MessageSent
import org.maxizenit.maxigram.chat.web.toBroadcastResponse
import org.springframework.context.event.EventListener
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Component

/** Pushes newly sent messages and chat-state changes to the chat's STOMP topics. */
@Component
class RealtimeMessageBroadcaster(private val messagingTemplate: SimpMessagingTemplate) {

    @EventListener
    fun onMessageSent(event: MessageSent) {
        messagingTemplate.convertAndSend(
            "/topic/chats/${event.chatId}",
            event.message.toBroadcastResponse(event.anonymous),
        )
    }

    /**
     * State frames are just triggers: each participant refetches the chat over REST and gets
     * their own requester-relative (and properly masked) view.
     */
    @EventListener
    fun onChatStateChanged(event: ChatStateChanged) {
        messagingTemplate.convertAndSend("/topic/chats/${event.chatId}/state", StateFrame())
    }

    data class StateFrame(val type: String = "state-changed")
}
