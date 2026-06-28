package org.maxizenit.maxigram.feed

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID

class PostServiceTest {

    private val posts = FakePostRepository()
    private val service = PostService(posts, Clock.fixed(Instant.EPOCH, ZoneOffset.UTC))

    @Test
    fun `creates a post with trimmed text`() {
        val post = service.create(UUID.randomUUID(), "  hello  ")

        assertThat(post.text).isEqualTo("hello")
        assertThat(posts.existsById(post.id)).isTrue()
    }

    @Test
    fun `rejects blank text`() {
        assertThatThrownBy { service.create(UUID.randomUUID(), "   ") }
            .isInstanceOf(InvalidContentException::class.java)
    }

    @Test
    fun `getView throws for a missing post`() {
        assertThatThrownBy { service.getView(404L, UUID.randomUUID()) }
            .isInstanceOf(PostNotFoundException::class.java)
    }
}
