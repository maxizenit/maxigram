package org.maxizenit.maxigram.identity.web

import org.maxizenit.maxigram.identity.PasswordResetService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

data class PasswordResetRequest(val email: String)

data class PasswordResetConfirmation(val password: String)

@RestController
@RequestMapping("/api/identity/password-resets")
class PasswordResetController(private val service: PasswordResetService) {

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun request(@RequestBody body: PasswordResetRequest) {
        service.requestReset(body.email)
    }

    @PostMapping("/{token}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun reset(@PathVariable token: UUID, @RequestBody body: PasswordResetConfirmation) {
        service.reset(token, body.password)
    }
}
