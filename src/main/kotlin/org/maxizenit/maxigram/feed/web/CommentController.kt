package org.maxizenit.maxigram.feed.web

import org.maxizenit.maxigram.common.CurrentUser
import org.maxizenit.maxigram.feed.Comment
import org.maxizenit.maxigram.feed.CommentService
import org.maxizenit.maxigram.feed.CommentView
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.time.Instant
import java.util.UUID

data class CommentRequest(val text: String)

data class CommentResponse(
    val id: Long,
    val postId: Long,
    val authorId: UUID,
    val text: String,
    val createdAt: Instant,
    val likesCount: Long,
    val likedByMe: Boolean,
)

private fun Comment.toResponse() = CommentResponse(id, postId, authorId, text, createdAt, 0, false)

private fun CommentView.toResponse() =
    CommentResponse(id, postId, authorId, text, createdAt, likesCount, likedByMe)

@RestController
@RequestMapping("/api/posts/{postId}/comments")
class CommentController(
    private val service: CommentService,
    private val currentUser: CurrentUser,
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun add(@PathVariable postId: Long, @RequestBody request: CommentRequest): CommentResponse =
        service.add(postId, currentUser.id(), request.text).toResponse()

    @GetMapping
    fun forPost(@PathVariable postId: Long): List<CommentResponse> =
        service.viewsForPost(postId, currentUser.id()).map { it.toResponse() }
}
