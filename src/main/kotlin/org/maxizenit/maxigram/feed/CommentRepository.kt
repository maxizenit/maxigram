package org.maxizenit.maxigram.feed

import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.maxizenit.maxigram.jooq.Tables.COMMENT
import org.maxizenit.maxigram.jooq.Tables.COMMENT_LIKE
import org.springframework.stereotype.Repository
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID

interface CommentRepository {
    fun insert(postId: Long, authorId: UUID, text: String, createdAt: Instant): Comment
    fun existsById(id: Long): Boolean
    fun viewsForPost(postId: Long, requesterId: UUID): List<CommentView>
}

@Repository
class JooqCommentRepository(private val dsl: DSLContext) : CommentRepository {

    override fun insert(postId: Long, authorId: UUID, text: String, createdAt: Instant): Comment {
        val id =
            dsl.insertInto(COMMENT)
                .set(COMMENT.POST_ID, postId)
                .set(COMMENT.AUTHOR_ID, authorId)
                .set(COMMENT.TEXT, text)
                .set(COMMENT.CREATED_AT, createdAt.atOffset(ZoneOffset.UTC))
                .returningResult(COMMENT.ID)
                .fetchOne()!!
                .value1()!!
        return Comment(id, postId, authorId, text, createdAt)
    }

    override fun existsById(id: Long): Boolean = dsl.fetchExists(COMMENT, COMMENT.ID.eq(id))

    override fun viewsForPost(postId: Long, requesterId: UUID): List<CommentView> =
        dsl.select(
            COMMENT.ID,
            COMMENT.POST_ID,
            COMMENT.AUTHOR_ID,
            COMMENT.TEXT,
            COMMENT.CREATED_AT,
            DSL.field(DSL.select(DSL.count()).from(COMMENT_LIKE).where(COMMENT_LIKE.COMMENT_ID.eq(COMMENT.ID))),
            DSL.field(
                DSL.exists(
                    DSL.selectOne()
                        .from(COMMENT_LIKE)
                        .where(COMMENT_LIKE.COMMENT_ID.eq(COMMENT.ID).and(COMMENT_LIKE.AUTHOR_ID.eq(requesterId))),
                ),
            ),
        )
            .from(COMMENT)
            .where(COMMENT.POST_ID.eq(postId))
            .orderBy(COMMENT.CREATED_AT.asc(), COMMENT.ID.asc())
            .fetch {
                CommentView(
                    id = it.value1(),
                    postId = it.value2(),
                    authorId = it.value3(),
                    text = it.value4(),
                    createdAt = it.value5().toInstant(),
                    likesCount = it.value6().toLong(),
                    likedByMe = it.value7(),
                )
            }
}
