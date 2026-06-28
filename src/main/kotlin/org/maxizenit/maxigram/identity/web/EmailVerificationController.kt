package org.maxizenit.maxigram.identity.web

import org.maxizenit.maxigram.identity.EmailVerificationService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/identity/email-verifications")
class EmailVerificationController(private val service: EmailVerificationService) {

    @PostMapping("/{token}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun verify(@PathVariable token: UUID) {
        service.verify(token)
    }
}
