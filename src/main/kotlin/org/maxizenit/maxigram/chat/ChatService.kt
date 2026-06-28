package org.maxizenit.maxigram.chat

import org.springframework.stereotype.Service
import java.time.Clock
import java.time.Instant
import java.util.UUID

@Service
class ChatService(
    private val chats: ChatRepository,
    private val clock: Clock = Clock.systemUTC(),
) {

    fun openChatWith(requesterId: UUID, otherId: UUID): Chat {
        if (requesterId == otherId) throw InvalidChatException("Cannot open a chat with yourself")
        return chats.findBetween(requesterId, otherId)
            ?: chats.insert(requesterId, otherId, Instant.now(clock))
    }

    /** Loads a chat only if [userId] participates in it (fixes the v1 IDOR). */
    fun requireParticipant(chatId: Long, userId: UUID): Chat {
        val chat = chats.findById(chatId) ?: throw ChatNotFoundException(chatId)
        if (userId != chat.firstParticipantId && userId != chat.secondParticipantId) {
            throw ChatAccessDeniedException(chatId, userId)
        }
        return chat
    }

    fun chatsOf(userId: UUID): List<ChatView> = chats.chatViewsFor(userId)
}
