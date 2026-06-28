package org.maxizenit.maxigram.feed

import org.jooq.DSLContext
import org.maxizenit.maxigram.jooq.Tables.COMMENT
import org.maxizenit.maxigram.jooq.Tables.POST_LIKE
import org.springframework.stereotype.Component
import java.util.UUID

/** Exposes a user's feed activity for the matching module. */
@Component
class FeedActivityProvider(private val dsl: DSLContext) {

    fun likedPostIds(userId: UUID): Set<Long> =
        dsl.select(POST_LIKE.POST_ID)
            .from(POST_LIKE)
            .where(POST_LIKE.AUTHOR_ID.eq(userId))
            .fetchSet(POST_LIKE.POST_ID)

    fun commentedPostIds(userId: UUID): Set<Long> =
        dsl.selectDistinct(COMMENT.POST_ID)
            .from(COMMENT)
            .where(COMMENT.AUTHOR_ID.eq(userId))
            .fetchSet(COMMENT.POST_ID)
}
