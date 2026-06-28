package org.maxizenit.maxigram.feed

import org.jooq.DSLContext
import org.maxizenit.maxigram.jooq.Tables.COMMENT_LIKE
import org.maxizenit.maxigram.jooq.Tables.POST_LIKE
import org.springframework.stereotype.Repository
import java.util.UUID

interface LikeRepository {
    /** Returns true if a new like was created, false if it already existed. */
    fun likePost(postId: Long, userId: UUID): Boolean
    fun unlikePost(postId: Long, userId: UUID)
    fun likeComment(commentId: Long, userId: UUID): Boolean
    fun unlikeComment(commentId: Long, userId: UUID)
}

@Repository
class JooqLikeRepository(private val dsl: DSLContext) : LikeRepository {

    override fun likePost(postId: Long, userId: UUID): Boolean =
        dsl.insertInto(POST_LIKE)
            .set(POST_LIKE.POST_ID, postId)
            .set(POST_LIKE.AUTHOR_ID, userId)
            .onConflictDoNothing()
            .execute() > 0

    override fun unlikePost(postId: Long, userId: UUID) {
        dsl.deleteFrom(POST_LIKE)
            .where(POST_LIKE.POST_ID.eq(postId).and(POST_LIKE.AUTHOR_ID.eq(userId)))
            .execute()
    }

    override fun likeComment(commentId: Long, userId: UUID): Boolean =
        dsl.insertInto(COMMENT_LIKE)
            .set(COMMENT_LIKE.COMMENT_ID, commentId)
            .set(COMMENT_LIKE.AUTHOR_ID, userId)
            .onConflictDoNothing()
            .execute() > 0

    override fun unlikeComment(commentId: Long, userId: UUID) {
        dsl.deleteFrom(COMMENT_LIKE)
            .where(COMMENT_LIKE.COMMENT_ID.eq(commentId).and(COMMENT_LIKE.AUTHOR_ID.eq(userId)))
            .execute()
    }
}
