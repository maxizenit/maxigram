package org.maxizenit.maxigram.feed

import org.springframework.stereotype.Service
import java.time.Clock
import java.time.Instant
import java.util.UUID

@Service
class CommentService(
    private val comments: CommentRepository,
    private val posts: PostRepository,
    private val clock: Clock = Clock.systemUTC(),
) {

    fun add(postId: Long, authorId: UUID, text: String): Comment {
        if (!posts.existsById(postId)) throw PostNotFoundException(postId)
        val trimmed = text.trim()
        if (trimmed.isEmpty()) throw InvalidContentException("Comment text must not be blank")
        return comments.insert(postId, authorId, trimmed, Instant.now(clock))
    }

    fun viewsForPost(postId: Long, requesterId: UUID): List<CommentView> {
        if (!posts.existsById(postId)) throw PostNotFoundException(postId)
        return comments.viewsForPost(postId, requesterId)
    }
}
