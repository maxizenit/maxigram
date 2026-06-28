package org.maxizenit.maxigram.feed

import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class LikeService(
    private val likes: LikeRepository,
    private val posts: PostRepository,
    private val comments: CommentRepository,
    private val events: ApplicationEventPublisher,
) {

    /** Idempotent (onConflictDoNothing); v1 lost likes entirely due to JPA cascade mistakes. */
    fun likePost(postId: Long, userId: UUID) {
        val authorId = posts.authorOf(postId) ?: throw PostNotFoundException(postId)
        if (likes.likePost(postId, userId)) {
            events.publishEvent(PostLiked(postId, authorId, userId))
        }
    }

    fun unlikePost(postId: Long, userId: UUID) = likes.unlikePost(postId, userId)

    fun likeComment(commentId: Long, userId: UUID) {
        if (!comments.existsById(commentId)) throw CommentNotFoundException(commentId)
        likes.likeComment(commentId, userId)
    }

    fun unlikeComment(commentId: Long, userId: UUID) = likes.unlikeComment(commentId, userId)
}
