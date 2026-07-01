package org.maxizenit.maxigram.chat

import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.impl.DSL
import org.maxizenit.maxigram.jooq.Tables.CHAT
import org.maxizenit.maxigram.jooq.Tables.MESSAGE
import org.springframework.stereotype.Repository
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID

interface ChatRepository {
    fun insert(firstParticipantId: UUID, secondParticipantId: UUID, createdAt: Instant): Chat
    fun insertAnonymous(firstParticipantId: UUID, secondParticipantId: UUID, createdAt: Instant): Chat
    fun findById(id: Long): Chat?
    /** Finds an existing non-anonymous chat between the two participants, in either order. */
    fun findRegularBetween(a: UUID, b: UUID): Chat?
    fun chatViewsFor(userId: UUID): List<ChatView>
    fun updateAnonymousState(chatId: Long, anonymous: Boolean, firstAgreed: Boolean, secondAgreed: Boolean, closed: Boolean)
}

@Repository
class JooqChatRepository(private val dsl: DSLContext) : ChatRepository {

    override fun insert(firstParticipantId: UUID, secondParticipantId: UUID, createdAt: Instant): Chat =
        insertChat(firstParticipantId, secondParticipantId, anonymous = false, createdAt)

    override fun insertAnonymous(firstParticipantId: UUID, secondParticipantId: UUID, createdAt: Instant): Chat =
        insertChat(firstParticipantId, secondParticipantId, anonymous = true, createdAt)

    private fun insertChat(first: UUID, second: UUID, anonymous: Boolean, createdAt: Instant): Chat {
        val id =
            dsl.insertInto(CHAT)
                .set(CHAT.FIRST_PARTICIPANT_ID, first)
                .set(CHAT.SECOND_PARTICIPANT_ID, second)
                .set(CHAT.ANONYMOUS, anonymous)
                .set(CHAT.CREATED_AT, createdAt.atOffset(ZoneOffset.UTC))
                .returningResult(CHAT.ID)
                .fetchOne()!!
                .value1()!!
        return Chat(id, first, second, createdAt, anonymous = anonymous)
    }

    override fun findById(id: Long): Chat? =
        selectChats().where(CHAT.ID.eq(id)).fetchOne()?.toChat()

    override fun findRegularBetween(a: UUID, b: UUID): Chat? =
        selectChats()
            .where(
                CHAT.ANONYMOUS.isFalse.and(
                    CHAT.FIRST_PARTICIPANT_ID.eq(a).and(CHAT.SECOND_PARTICIPANT_ID.eq(b))
                        .or(CHAT.FIRST_PARTICIPANT_ID.eq(b).and(CHAT.SECOND_PARTICIPANT_ID.eq(a))),
                ),
            )
            .limit(1)
            .fetchOne()
            ?.toChat()

    override fun chatViewsFor(userId: UUID): List<ChatView> =
        dsl.select(
            CHAT.ID,
            CHAT.FIRST_PARTICIPANT_ID,
            CHAT.SECOND_PARTICIPANT_ID,
            CHAT.ANONYMOUS,
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
                ChatView(it.value1(), it.value2(), it.value3(), it.value4(), it.value5().toInstant(), it.value6())
            }

    override fun updateAnonymousState(
        chatId: Long,
        anonymous: Boolean,
        firstAgreed: Boolean,
        secondAgreed: Boolean,
        closed: Boolean,
    ) {
        dsl.update(CHAT)
            .set(CHAT.ANONYMOUS, anonymous)
            .set(CHAT.FIRST_AGREED, firstAgreed)
            .set(CHAT.SECOND_AGREED, secondAgreed)
            .set(CHAT.CLOSED, closed)
            .where(CHAT.ID.eq(chatId))
            .execute()
    }

    private fun selectChats() =
        dsl.select(
            CHAT.ID,
            CHAT.FIRST_PARTICIPANT_ID,
            CHAT.SECOND_PARTICIPANT_ID,
            CHAT.CREATED_AT,
            CHAT.ANONYMOUS,
            CHAT.FIRST_AGREED,
            CHAT.SECOND_AGREED,
            CHAT.CLOSED,
        ).from(CHAT)

    private fun Record.toChat() =
        Chat(
            id = this[CHAT.ID],
            firstParticipantId = this[CHAT.FIRST_PARTICIPANT_ID],
            secondParticipantId = this[CHAT.SECOND_PARTICIPANT_ID],
            createdAt = this[CHAT.CREATED_AT].toInstant(),
            anonymous = this[CHAT.ANONYMOUS],
            firstAgreed = this[CHAT.FIRST_AGREED],
            secondAgreed = this[CHAT.SECOND_AGREED],
            closed = this[CHAT.CLOSED],
        )
}
