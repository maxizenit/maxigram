package org.maxizenit.maxigram.feed.web

import org.maxizenit.maxigram.feed.CommentNotFoundException
import org.maxizenit.maxigram.feed.InvalidContentException
import org.maxizenit.maxigram.feed.PostNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class FeedExceptionHandler {

    @ExceptionHandler(PostNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handlePostNotFound(e: PostNotFoundException): Map<String, String?> =
        mapOf("error" to e.message)

    @ExceptionHandler(CommentNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleCommentNotFound(e: CommentNotFoundException): Map<String, String?> =
        mapOf("error" to e.message)

    @ExceptionHandler(InvalidContentException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleInvalidContent(e: InvalidContentException): Map<String, String?> =
        mapOf("error" to e.message)
}
