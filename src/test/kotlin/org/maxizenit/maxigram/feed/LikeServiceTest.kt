package org.maxizenit.maxigram.feed

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.context.ApplicationEventPublisher
import java.time.Instant
import java.util.UUID

class LikeServiceTest {

    private val likes = FakeLikeRepository()
    private val posts = FakePostRepository()
    private val comments = FakeCommentRepository()
    private val service = LikeService(likes, posts, comments, ApplicationEventPublisher { })

    private val post = posts.insert(UUID.randomUUID(), "post", Instant.EPOCH)
    private val comment = comments.insert(post.id, UUID.randomUUID(), "comment", Instant.EPOCH)
    private val user = UUID.randomUUID()

    @Test
    fun `liking a post is recorded and idempotent`() {
        service.likePost(post.id, user)
        service.likePost(post.id, user)

        assertThat(likes.postLikes).containsExactly(post.id to user)
    }

    @Test
    fun `unliking a post removes the like`() {
        service.likePost(post.id, user)
        service.unlikePost(post.id, user)

        assertThat(likes.postLikes).isEmpty()
    }

    @Test
    fun `liking a missing post fails`() {
        assertThatThrownBy { service.likePost(404L, user) }
            .isInstanceOf(PostNotFoundException::class.java)
    }

    @Test
    fun `liking a missing comment fails`() {
        assertThatThrownBy { service.likeComment(404L, user) }
            .isInstanceOf(CommentNotFoundException::class.java)
    }

    @Test
    fun `liking a comment is recorded`() {
        service.likeComment(comment.id, user)

        assertThat(likes.commentLikes).containsExactly(comment.id to user)
    }
}
