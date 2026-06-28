package org.maxizenit.maxigram.profile.web

import org.maxizenit.maxigram.profile.ProfileService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/interests")
class InterestController(private val service: ProfileService) {

    @GetMapping
    fun list(): List<InterestResponse> =
        service.listInterests().map { InterestResponse(it.id, it.name) }
}
