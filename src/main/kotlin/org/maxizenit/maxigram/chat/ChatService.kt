package org.maxizenit.maxigram.chat

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
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
        return chats.findRegularBetween(requesterId, otherId)
            ?: chats.insert(requesterId, otherId, Instant.now(clock))
    }

    /** Creates an anonymous chat between two matched users (used by the matching module). */
    fun createAnonymousChat(firstParticipantId: UUID, secondParticipantId: UUID): Chat =
        chats.insertAnonymous(firstParticipantId, secondParticipantId, Instant.now(clock))

    /** Loads a chat only if [userId] participates in it (fixes the v1 IDOR). */
    fun requireParticipant(chatId: Long, userId: UUID): Chat {
        val chat = chats.findById(chatId) ?: throw ChatNotFoundException(chatId)
        if (userId != chat.firstParticipantId && userId != chat.secondParticipantId) {
            throw ChatAccessDeniedException(chatId, userId)
        }
        return chat
    }

    fun chatsOf(userId: UUID): List<ChatView> = chats.chatViewsFor(userId)

    /**
     * Records [requesterId]'s consent to de-anonymization. When both participants have agreed, a
     * regular chat between them is found-or-created and linked via [Chat.newChatId].
     *
     * Fixes the v1 bug where the second participant's consent set the flag to `false` and
     * `newChatId` was taken from the wrong field.
     */
    @Transactional
    fun agreeToDeAnonymization(chatId: Long, requesterId: UUID): Chat {
        val chat = requireParticipant(chatId, requesterId)
        if (!chat.anonymous) throw InvalidChatException("Chat is not anonymous")
        if (chat.closed) throw InvalidChatException("Chat is closed")

        val requesterIsFirst = chat.firstParticipantId == requesterId
        val firstAgreed = if (requesterIsFirst) true else chat.firstAgreed
        val secondAgreed = if (requesterIsFirst) chat.secondAgreed else true

        var newChatId = chat.newChatId
        if (firstAgreed && secondAgreed && newChatId == null) {
            newChatId = openChatWith(chat.firstParticipantId, chat.secondParticipantId).id
        }

        chats.updateAnonymousState(chatId, firstAgreed, secondAgreed, chat.closed, newChatId)
        return chat.copy(firstAgreed = firstAgreed, secondAgreed = secondAgreed, newChatId = newChatId)
    }

    fun closeAnonymousChat(chatId: Long, requesterId: UUID): Chat {
        val chat = requireParticipant(chatId, requesterId)
        if (!chat.anonymous) throw InvalidChatException("Chat is not anonymous")
        if (chat.closed) throw InvalidChatException("Chat is already closed")

        chats.updateAnonymousState(chatId, chat.firstAgreed, chat.secondAgreed, closed = true, chat.newChatId)
        return chat.copy(closed = true)
    }
}
