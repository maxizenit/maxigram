package org.maxizenit.maxigram.identity.web

import org.maxizenit.maxigram.identity.EmailAlreadyUsedException
import org.maxizenit.maxigram.identity.WeakPasswordException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class IdentityExceptionHandler {

    @ExceptionHandler(EmailAlreadyUsedException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun handleEmailAlreadyUsed(e: EmailAlreadyUsedException): Map<String, String?> =
        mapOf("error" to e.message)

    @ExceptionHandler(WeakPasswordException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleWeakPassword(e: WeakPasswordException): Map<String, String?> =
        mapOf("error" to e.message)
}
