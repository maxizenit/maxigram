package org.maxizenit.maxigram.feed.web

import org.maxizenit.maxigram.common.CurrentUser
import org.maxizenit.maxigram.feed.LikeService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
class LikeController(
    private val likeService: LikeService,
    private val currentUser: CurrentUser,
) {

    @PostMapping("/api/posts/{postId}/likes")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun likePost(@PathVariable postId: Long) = likeService.likePost(postId, currentUser.id())

    @DeleteMapping("/api/posts/{postId}/likes")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun unlikePost(@PathVariable postId: Long) = likeService.unlikePost(postId, currentUser.id())

    @PostMapping("/api/comments/{commentId}/likes")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun likeComment(@PathVariable commentId: Long) = likeService.likeComment(commentId, currentUser.id())

    @DeleteMapping("/api/comments/{commentId}/likes")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun unlikeComment(@PathVariable commentId: Long) = likeService.unlikeComment(commentId, currentUser.id())
}
