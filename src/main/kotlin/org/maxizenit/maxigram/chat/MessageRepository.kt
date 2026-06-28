package org.maxizenit.maxigram.chat

import org.jooq.DSLContext
import org.maxizenit.maxigram.jooq.Tables.MESSAGE
import org.springframework.stereotype.Repository
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID

interface MessageRepository {
    fun insert(chatId: Long, senderId: UUID, text: String, createdAt: Instant): Message
    fun findByChatId(chatId: Long): List<Message>
    fun markReadFromOthers(chatId: Long, readerId: UUID)
}

@Repository
class JooqMessageRepository(private val dsl: DSLContext) : MessageRepository {

    override fun insert(chatId: Long, senderId: UUID, text: String, createdAt: Instant): Message {
        val id =
            dsl.insertInto(MESSAGE)
                .set(MESSAGE.CHAT_ID, chatId)
                .set(MESSAGE.SENDER_ID, senderId)
                .set(MESSAGE.TEXT, text)
                .set(MESSAGE.CREATED_AT, createdAt.atOffset(ZoneOffset.UTC))
                .set(MESSAGE.READ, false)
                .returningResult(MESSAGE.ID)
                .fetchOne()!!
                .value1()!!
        return Message(id, chatId, senderId, text, createdAt, read = false)
    }

    override fun findByChatId(chatId: Long): List<Message> =
        dsl.select(MESSAGE.ID, MESSAGE.CHAT_ID, MESSAGE.SENDER_ID, MESSAGE.TEXT, MESSAGE.CREATED_AT, MESSAGE.READ)
            .from(MESSAGE)
            .where(MESSAGE.CHAT_ID.eq(chatId))
            .orderBy(MESSAGE.CREATED_AT.asc(), MESSAGE.ID.asc())
            .fetch {
                Message(
                    it[MESSAGE.ID],
                    it[MESSAGE.CHAT_ID],
                    it[MESSAGE.SENDER_ID],
                    it[MESSAGE.TEXT],
                    it[MESSAGE.CREATED_AT].toInstant(),
                    it[MESSAGE.READ],
                )
            }

    override fun markReadFromOthers(chatId: Long, readerId: UUID) {
        dsl.update(MESSAGE)
            .set(MESSAGE.READ, true)
            .where(MESSAGE.CHAT_ID.eq(chatId).and(MESSAGE.SENDER_ID.ne(readerId)).and(MESSAGE.READ.isFalse))
            .execute()
    }
}
