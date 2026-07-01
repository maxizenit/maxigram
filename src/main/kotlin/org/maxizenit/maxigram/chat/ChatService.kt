package org.maxizenit.maxigram.chat

import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.Instant
import java.util.UUID

@Service
class ChatService(
    private val chats: ChatRepository,
    private val events: ApplicationEventPublisher,
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
     * Records [requesterId]'s consent to de-anonymization. Once both participants have agreed,
     * the chat is converted to a regular one in place: identities are revealed but the thread
     * (and its history) stays the same. Retroactively unmasking history is safe in a 1-on-1
     * chat -- after the reveal, authorship is derivable anyway.
     *
     * Fixes the v1 bug where the second participant's consent reset the first one's flag.
     */
    @Transactional
    fun agreeToDeAnonymization(chatId: Long, requesterId: UUID): Chat {
        val chat = requireParticipant(chatId, requesterId)
        if (!chat.anonymous) throw InvalidChatException("Chat is not anonymous")
        if (chat.closed) throw InvalidChatException("Chat is closed")

        val requesterIsFirst = chat.firstParticipantId == requesterId
        val firstAgreed = if (requesterIsFirst) true else chat.firstAgreed
        val secondAgreed = if (requesterIsFirst) chat.secondAgreed else true
        val anonymous = !(firstAgreed && secondAgreed)

        chats.updateAnonymousState(chatId, anonymous, firstAgreed, secondAgreed, chat.closed)
        events.publishEvent(ChatStateChanged(chatId))
        return chat.copy(anonymous = anonymous, firstAgreed = firstAgreed, secondAgreed = secondAgreed)
    }

    fun closeAnonymousChat(chatId: Long, requesterId: UUID): Chat {
        val chat = requireParticipant(chatId, requesterId)
        if (!chat.anonymous) throw InvalidChatException("Chat is not anonymous")
        if (chat.closed) throw InvalidChatException("Chat is already closed")

        chats.updateAnonymousState(chatId, chat.anonymous, chat.firstAgreed, chat.secondAgreed, closed = true)
        events.publishEvent(ChatStateChanged(chatId))
        return chat.copy(closed = true)
    }
}
