package org.maxizenit.maxigram.identity.web

import org.maxizenit.maxigram.identity.CurrentUser
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

data class MeResponse(val id: UUID)

@RestController
@RequestMapping("/api/identity/me")
class MeController(private val currentUser: CurrentUser) {

    @GetMapping
    fun me(): MeResponse = MeResponse(currentUser.id())
}
