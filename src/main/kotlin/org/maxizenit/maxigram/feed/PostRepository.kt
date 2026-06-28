package org.maxizenit.maxigram.feed

import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.maxizenit.maxigram.jooq.Tables.COMMENT
import org.maxizenit.maxigram.jooq.Tables.POST
import org.maxizenit.maxigram.jooq.Tables.POST_LIKE
import org.springframework.stereotype.Repository
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID

interface PostRepository {
    fun insert(authorId: UUID, text: String, createdAt: Instant): Post
    fun existsById(id: Long): Boolean
    fun authorOf(id: Long): UUID?
    fun viewById(id: Long, requesterId: UUID): PostView?
    fun feedView(authorIds: Collection<UUID>, requesterId: UUID): List<PostView>
}

@Repository
class JooqPostRepository(private val dsl: DSLContext) : PostRepository {

    override fun insert(authorId: UUID, text: String, createdAt: Instant): Post {
        val id =
            dsl.insertInto(POST)
                .set(POST.AUTHOR_ID, authorId)
                .set(POST.TEXT, text)
                .set(POST.CREATED_AT, createdAt.atOffset(ZoneOffset.UTC))
                .returningResult(POST.ID)
                .fetchOne()!!
                .value1()!!
        return Post(id, authorId, text, createdAt)
    }

    override fun existsById(id: Long): Boolean = dsl.fetchExists(POST, POST.ID.eq(id))

    override fun authorOf(id: Long): UUID? =
        dsl.select(POST.AUTHOR_ID).from(POST).where(POST.ID.eq(id)).fetchOne(POST.AUTHOR_ID)

    override fun viewById(id: Long, requesterId: UUID): PostView? =
        viewQuery(requesterId).where(POST.ID.eq(id)).fetchOne { it.toPostView() }

    override fun feedView(authorIds: Collection<UUID>, requesterId: UUID): List<PostView> {
        if (authorIds.isEmpty()) return emptyList()
        return viewQuery(requesterId)
            .where(POST.AUTHOR_ID.`in`(authorIds))
            .orderBy(POST.CREATED_AT.desc(), POST.ID.desc())
            .fetch { it.toPostView() }
    }

    // Single query: counters and like-state are correlated subqueries, so a feed of N posts
    // is still one round-trip (no N+1, fixing the v1 defect).
    private fun viewQuery(requesterId: UUID) =
        dsl.select(
            POST.ID,
            POST.AUTHOR_ID,
            POST.TEXT,
            POST.CREATED_AT,
            DSL.field(DSL.select(DSL.count()).from(POST_LIKE).where(POST_LIKE.POST_ID.eq(POST.ID))),
            DSL.field(DSL.select(DSL.count()).from(COMMENT).where(COMMENT.POST_ID.eq(POST.ID))),
            DSL.field(
                DSL.exists(
                    DSL.selectOne()
                        .from(POST_LIKE)
                        .where(POST_LIKE.POST_ID.eq(POST.ID).and(POST_LIKE.AUTHOR_ID.eq(requesterId))),
                ),
            ),
        ).from(POST)

    private fun org.jooq.Record7<Long, UUID, String, java.time.OffsetDateTime, Int, Int, Boolean>.toPostView() =
        PostView(
            id = value1(),
            authorId = value2(),
            text = value3(),
            createdAt = value4().toInstant(),
            likesCount = value5().toLong(),
            commentsCount = value6().toLong(),
            likedByMe = value7(),
        )
}
