package org.maxizenit.maxigram.feed

import java.time.Instant
import java.util.UUID

data class Post(
    val id: Long,
    val authorId: UUID,
    val text: String,
    val createdAt: Instant,
)

data class Comment(
    val id: Long,
    val postId: Long,
    val authorId: UUID,
    val text: String,
    val createdAt: Instant,
)

/** Read model: a post enriched with aggregate counters and the caller's like state. */
data class PostView(
    val id: Long,
    val authorId: UUID,
    val text: String,
    val createdAt: Instant,
    val likesCount: Long,
    val commentsCount: Long,
    val likedByMe: Boolean,
)

data class CommentView(
    val id: Long,
    val postId: Long,
    val authorId: UUID,
    val text: String,
    val createdAt: Instant,
    val likesCount: Long,
    val likedByMe: Boolean,
)

class PostNotFoundException(postId: Long) : RuntimeException("No post with id $postId")

class CommentNotFoundException(commentId: Long) : RuntimeException("No comment with id $commentId")

class InvalidContentException(message: String) : RuntimeException(message)

/** Published when a post is newly liked (not republished for a duplicate like). */
data class PostLiked(val postId: Long, val authorId: UUID, val likerId: UUID)

/** Published when a comment is added to a post. */
data class PostCommented(val postId: Long, val authorId: UUID, val commenterId: UUID)
