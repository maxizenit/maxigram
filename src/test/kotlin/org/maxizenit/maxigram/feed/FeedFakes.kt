package org.maxizenit.maxigram.feed

import java.time.Instant
import java.util.UUID

class FakePostRepository : PostRepository {

    val posts = mutableListOf<Post>()
    private var seq = 0L

    override fun insert(authorId: UUID, text: String, createdAt: Instant): Post {
        val post = Post(++seq, authorId, text, createdAt)
        posts.add(post)
        return post
    }

    override fun existsById(id: Long): Boolean = posts.any { it.id == id }

    override fun viewById(id: Long, requesterId: UUID): PostView? =
        posts.firstOrNull { it.id == id }?.toView()

    override fun feedView(authorIds: Collection<UUID>, requesterId: UUID): List<PostView> =
        posts.filter { it.authorId in authorIds }.sortedByDescending { it.createdAt }.map { it.toView() }

    fun seed(post: Post) {
        posts.add(post)
    }

    private fun Post.toView() = PostView(id, authorId, text, createdAt, 0, 0, false)
}

class FakeCommentRepository : CommentRepository {

    val comments = mutableListOf<Comment>()
    private var seq = 0L

    override fun insert(postId: Long, authorId: UUID, text: String, createdAt: Instant): Comment {
        val comment = Comment(++seq, postId, authorId, text, createdAt)
        comments.add(comment)
        return comment
    }

    override fun existsById(id: Long): Boolean = comments.any { it.id == id }

    override fun viewsForPost(postId: Long, requesterId: UUID): List<CommentView> =
        comments.filter { it.postId == postId }
            .sortedBy { it.createdAt }
            .map { CommentView(it.id, it.postId, it.authorId, it.text, it.createdAt, 0, false) }
}

class FakeLikeRepository : LikeRepository {

    val postLikes = linkedSetOf<Pair<Long, UUID>>()
    val commentLikes = linkedSetOf<Pair<Long, UUID>>()

    override fun likePost(postId: Long, userId: UUID) {
        postLikes.add(postId to userId)
    }

    override fun unlikePost(postId: Long, userId: UUID) {
        postLikes.remove(postId to userId)
    }

    override fun likeComment(commentId: Long, userId: UUID) {
        commentLikes.add(commentId to userId)
    }

    override fun unlikeComment(commentId: Long, userId: UUID) {
        commentLikes.remove(commentId to userId)
    }
}
