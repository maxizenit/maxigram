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
        chatService.requireParticipant(chatId, senderId)
        val trimmed = text.trim()
        if (trimmed.isEmpty()) throw InvalidChatException("Message text must not be blank")
        val message = messages.insert(chatId, senderId, trimmed, Instant.now(clock))
        events.publishEvent(MessageSent(chatId, message))
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
