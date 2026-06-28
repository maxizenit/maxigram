package org.maxizenit.maxigram.wellbeing.web

import org.maxizenit.maxigram.wellbeing.InvalidRestraintException
import org.maxizenit.maxigram.wellbeing.RestraintActiveException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class WellbeingExceptionHandler {

    @ExceptionHandler(InvalidRestraintException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleInvalid(e: InvalidRestraintException): Map<String, String?> =
        mapOf("error" to e.message)

    @ExceptionHandler(RestraintActiveException::class)
    @ResponseStatus(HttpStatus.LOCKED)
    fun handleActive(e: RestraintActiveException): Map<String, String?> =
        mapOf("error" to e.message)
}
