package org.maxizenit.maxigram.chat

import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.maxizenit.maxigram.jooq.Tables.CHAT
import org.maxizenit.maxigram.jooq.Tables.MESSAGE
import org.springframework.stereotype.Repository
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID

interface ChatRepository {
    fun insert(firstParticipantId: UUID, secondParticipantId: UUID, createdAt: Instant): Chat
    fun findById(id: Long): Chat?
    fun findBetween(a: UUID, b: UUID): Chat?
    fun chatViewsFor(userId: UUID): List<ChatView>
}

@Repository
class JooqChatRepository(private val dsl: DSLContext) : ChatRepository {

    override fun insert(firstParticipantId: UUID, secondParticipantId: UUID, createdAt: Instant): Chat {
        val id =
            dsl.insertInto(CHAT)
                .set(CHAT.FIRST_PARTICIPANT_ID, firstParticipantId)
                .set(CHAT.SECOND_PARTICIPANT_ID, secondParticipantId)
                .set(CHAT.CREATED_AT, createdAt.atOffset(ZoneOffset.UTC))
                .returningResult(CHAT.ID)
                .fetchOne()!!
                .value1()!!
        return Chat(id, firstParticipantId, secondParticipantId, createdAt)
    }

    override fun findById(id: Long): Chat? =
        dsl.select(CHAT.ID, CHAT.FIRST_PARTICIPANT_ID, CHAT.SECOND_PARTICIPANT_ID, CHAT.CREATED_AT)
            .from(CHAT)
            .where(CHAT.ID.eq(id))
            .fetchOne()
            ?.let { Chat(it[CHAT.ID], it[CHAT.FIRST_PARTICIPANT_ID], it[CHAT.SECOND_PARTICIPANT_ID], it[CHAT.CREATED_AT].toInstant()) }

    override fun findBetween(a: UUID, b: UUID): Chat? =
        dsl.select(CHAT.ID, CHAT.FIRST_PARTICIPANT_ID, CHAT.SECOND_PARTICIPANT_ID, CHAT.CREATED_AT)
            .from(CHAT)
            .where(
                CHAT.FIRST_PARTICIPANT_ID.eq(a).and(CHAT.SECOND_PARTICIPANT_ID.eq(b))
                    .or(CHAT.FIRST_PARTICIPANT_ID.eq(b).and(CHAT.SECOND_PARTICIPANT_ID.eq(a))),
            )
            .limit(1)
            .fetchOne()
            ?.let { Chat(it[CHAT.ID], it[CHAT.FIRST_PARTICIPANT_ID], it[CHAT.SECOND_PARTICIPANT_ID], it[CHAT.CREATED_AT].toInstant()) }

    override fun chatViewsFor(userId: UUID): List<ChatView> =
        dsl.select(
            CHAT.ID,
            CHAT.FIRST_PARTICIPANT_ID,
            CHAT.SECOND_PARTICIPANT_ID,
            CHAT.CREATED_AT,
            DSL.field(
                DSL.select(MESSAGE.TEXT)
                    .from(MESSAGE)
                    .where(MESSAGE.CHAT_ID.eq(CHAT.ID))
                    .orderBy(MESSAGE.CREATED_AT.desc(), MESSAGE.ID.desc())
                    .limit(1),
            ),
        )
            .from(CHAT)
            .where(CHAT.FIRST_PARTICIPANT_ID.eq(userId).or(CHAT.SECOND_PARTICIPANT_ID.eq(userId)))
            .orderBy(CHAT.CREATED_AT.desc())
            .fetch {
                ChatView(it.value1(), it.value2(), it.value3(), it.value4().toInstant(), it.value5())
            }
}
