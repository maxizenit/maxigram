package org.maxizenit.maxigram.chat

import java.time.Instant
import java.util.UUID

data class Chat(
    val id: Long,
    val firstParticipantId: UUID,
    val secondParticipantId: UUID,
    val createdAt: Instant,
)

/** A chat enriched with the text of its most recent message, for chat lists. */
data class ChatView(
    val id: Long,
    val firstParticipantId: UUID,
    val secondParticipantId: UUID,
    val createdAt: Instant,
    val lastMessage: String?,
)

data class Message(
    val id: Long,
    val chatId: Long,
    val senderId: UUID,
    val text: String,
    val createdAt: Instant,
    val read: Boolean,
)

class ChatNotFoundException(chatId: Long) : RuntimeException("No chat with id $chatId")

class ChatAccessDeniedException(chatId: Long, userId: UUID) :
    RuntimeException("User $userId is not a participant of chat $chatId")

class InvalidChatException(message: String) : RuntimeException(message)
