package org.maxizenit.maxigram.identity.web

import org.maxizenit.maxigram.identity.EmailVerificationService
import org.maxizenit.maxigram.identity.UserAccountService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

data class RegistrationRequest(val email: String, val password: String)

data class RegistrationResponse(val id: UUID, val email: String)

@RestController
@RequestMapping("/api/identity/registrations")
class RegistrationController(
    private val userAccountService: UserAccountService,
    private val emailVerificationService: EmailVerificationService,
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun register(@RequestBody request: RegistrationRequest): RegistrationResponse {
        val user = userAccountService.register(request.email, request.password)
        emailVerificationService.sendVerificationEmail(user.id, user.email)
        return RegistrationResponse(user.id, user.email)
    }
}
