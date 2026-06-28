package org.maxizenit.maxigram.chat

import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.Instant
import java.util.UUID

@Service
class MessageService(
    private val messages: MessageRepository,
    private val chatService: ChatService,
    private val events: ApplicationEventPublisher,
    private val clock: Clock = Clock.systemUTC(),
) {

    fun send(chatId: Long, senderId: UUID, text: String): Message {
        val chat = chatService.requireParticipant(chatId, senderId)
        val trimmed = text.trim()
        if (trimmed.isEmpty()) throw InvalidChatException("Message text must not be blank")
        val message = messages.insert(chatId, senderId, trimmed, Instant.now(clock))
        val recipientId =
            if (chat.firstParticipantId == senderId) chat.secondParticipantId else chat.firstParticipantId
        events.publishEvent(MessageSent(chatId, recipientId, message))
        return message
    }

    /** Returns the chat's messages, marking those from the other participant as read. */
    @Transactional
    fun readConversation(chatId: Long, requesterId: UUID): List<Message> {
        chatService.requireParticipant(chatId, requesterId)
        messages.markReadFromOthers(chatId, requesterId)
        return messages.findByChatId(chatId)
    }
}
