package org.maxizenit.maxigram.feed

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID

class CommentServiceTest {

    private val comments = FakeCommentRepository()
    private val posts = FakePostRepository()
    private val service = CommentService(comments, posts, Clock.fixed(Instant.EPOCH, ZoneOffset.UTC))

    private val post = posts.insert(UUID.randomUUID(), "post", Instant.EPOCH)

    @Test
    fun `adds a comment to an existing post`() {
        val comment = service.add(post.id, UUID.randomUUID(), "  nice  ")

        assertThat(comment.text).isEqualTo("nice")
        val views = service.viewsForPost(post.id, UUID.randomUUID())
        assertThat(views).hasSize(1)
        assertThat(views[0].text).isEqualTo("nice")
    }

    @Test
    fun `rejects a comment on a missing post`() {
        assertThatThrownBy { service.add(404L, UUID.randomUUID(), "hi") }
            .isInstanceOf(PostNotFoundException::class.java)
    }

    @Test
    fun `rejects blank comment text`() {
        assertThatThrownBy { service.add(post.id, UUID.randomUUID(), "  ") }
            .isInstanceOf(InvalidContentException::class.java)
    }
}
