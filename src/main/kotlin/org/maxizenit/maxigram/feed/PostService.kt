package org.maxizenit.maxigram.feed

import org.springframework.stereotype.Service
import java.time.Clock
import java.time.Instant
import java.util.UUID

@Service
class PostService(
    private val posts: PostRepository,
    private val clock: Clock = Clock.systemUTC(),
) {

    fun create(authorId: UUID, text: String): Post {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) throw InvalidContentException("Post text must not be blank")
        return posts.insert(authorId, trimmed, Instant.now(clock))
    }

    fun getView(id: Long, requesterId: UUID): PostView =
        posts.viewById(id, requesterId) ?: throw PostNotFoundException(id)
}
