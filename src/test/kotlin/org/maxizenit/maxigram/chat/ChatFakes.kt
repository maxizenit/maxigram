package org.maxizenit.maxigram.chat

import java.time.Instant
import java.util.UUID

class FakeChatRepository : ChatRepository {

    val chats = mutableListOf<Chat>()
    private var seq = 0L

    override fun insert(firstParticipantId: UUID, secondParticipantId: UUID, createdAt: Instant): Chat {
        val chat = Chat(++seq, firstParticipantId, secondParticipantId, createdAt)
        chats.add(chat)
        return chat
    }

    override fun insertAnonymous(firstParticipantId: UUID, secondParticipantId: UUID, createdAt: Instant): Chat {
        val chat = Chat(++seq, firstParticipantId, secondParticipantId, createdAt, anonymous = true)
        chats.add(chat)
        return chat
    }

    override fun findById(id: Long): Chat? = chats.firstOrNull { it.id == id }

    override fun findRegularBetween(a: UUID, b: UUID): Chat? =
        chats.firstOrNull {
            !it.anonymous &&
                ((it.firstParticipantId == a && it.secondParticipantId == b) ||
                    (it.firstParticipantId == b && it.secondParticipantId == a))
        }

    override fun chatViewsFor(userId: UUID): List<ChatView> =
        chats.filter { it.firstParticipantId == userId || it.secondParticipantId == userId }
            .map { ChatView(it.id, it.firstParticipantId, it.secondParticipantId, it.anonymous, it.createdAt, null) }

    override fun updateAnonymousState(
        chatId: Long,
        anonymous: Boolean,
        firstAgreed: Boolean,
        secondAgreed: Boolean,
        closed: Boolean,
    ) {
        val index = chats.indexOfFirst { it.id == chatId }
        if (index >= 0) {
            chats[index] = chats[index].copy(
                anonymous = anonymous,
                firstAgreed = firstAgreed,
                secondAgreed = secondAgreed,
                closed = closed,
            )
        }
    }
}

class FakeMessageRepository : MessageRepository {

    val messages = mutableListOf<Message>()
    private var seq = 0L

    override fun insert(chatId: Long, senderId: UUID, text: String, createdAt: Instant): Message {
        val message = Message(++seq, chatId, senderId, text, createdAt, read = false)
        messages.add(message)
        return message
    }

    override fun findByChatId(chatId: Long): List<Message> =
        messages.filter { it.chatId == chatId }.sortedBy { it.createdAt }

    override fun markReadFromOthers(chatId: Long, readerId: UUID) {
        messages.replaceAll {
            if (it.chatId == chatId && it.senderId != readerId && !it.read) it.copy(read = true) else it
        }
    }
}
