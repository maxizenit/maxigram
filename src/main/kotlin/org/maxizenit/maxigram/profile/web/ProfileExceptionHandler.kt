package org.maxizenit.maxigram.profile.web

import org.maxizenit.maxigram.profile.InvalidProfileException
import org.maxizenit.maxigram.profile.InvalidSubscriptionException
import org.maxizenit.maxigram.profile.ProfileNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ProfileExceptionHandler {

    @ExceptionHandler(InvalidProfileException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleInvalidProfile(e: InvalidProfileException): Map<String, String?> =
        mapOf("error" to e.message)

    @ExceptionHandler(ProfileNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleProfileNotFound(e: ProfileNotFoundException): Map<String, String?> =
        mapOf("error" to e.message)

    @ExceptionHandler(InvalidSubscriptionException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleInvalidSubscription(e: InvalidSubscriptionException): Map<String, String?> =
        mapOf("error" to e.message)
}
