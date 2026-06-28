package org.maxizenit.maxigram.chat.web

import org.maxizenit.maxigram.chat.ChatAccessDeniedException
import org.maxizenit.maxigram.chat.ChatNotFoundException
import org.maxizenit.maxigram.chat.InvalidChatException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ChatExceptionHandler {

    @ExceptionHandler(ChatNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleChatNotFound(e: ChatNotFoundException): Map<String, String?> =
        mapOf("error" to e.message)

    @ExceptionHandler(ChatAccessDeniedException::class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    fun handleAccessDenied(e: ChatAccessDeniedException): Map<String, String?> =
        mapOf("error" to e.message)

    @ExceptionHandler(InvalidChatException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleInvalidChat(e: InvalidChatException): Map<String, String?> =
        mapOf("error" to e.message)
}
