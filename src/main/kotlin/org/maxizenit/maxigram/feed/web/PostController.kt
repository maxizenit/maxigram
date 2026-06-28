package org.maxizenit.maxigram.feed.web

import org.maxizenit.maxigram.common.CurrentUser
import org.maxizenit.maxigram.feed.Post
import org.maxizenit.maxigram.feed.PostService
import org.maxizenit.maxigram.feed.PostView
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

data class PostRequest(val text: String)

data class PostResponse(
    val id: Long,
    val authorId: UUID,
    val text: String,
    val createdAt: Instant,
    val likesCount: Long,
    val commentsCount: Long,
    val likedByMe: Boolean,
)

fun Post.toResponse() = PostResponse(id, authorId, text, createdAt, 0, 0, false)

fun PostView.toResponse() = PostResponse(id, authorId, text, createdAt, likesCount, commentsCount, likedByMe)

@RestController
@RequestMapping("/api/posts")
class PostController(
    private val service: PostService,
    private val currentUser: CurrentUser,
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody request: PostRequest): PostResponse =
        service.create(currentUser.id(), request.text).toResponse()

    @GetMapping("/{id}")
    fun byId(@PathVariable id: Long): PostResponse =
        service.getView(id, currentUser.id()).toResponse()
}
