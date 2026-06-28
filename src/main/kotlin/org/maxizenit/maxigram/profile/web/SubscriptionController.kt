package org.maxizenit.maxigram.profile.web

import org.maxizenit.maxigram.common.CurrentUser
import org.maxizenit.maxigram.profile.SubscriptionService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/subscriptions")
class SubscriptionController(
    private val service: SubscriptionService,
    private val currentUser: CurrentUser,
) {

    @PostMapping("/{authorId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun subscribe(@PathVariable authorId: UUID) {
        service.subscribe(currentUser.id(), authorId)
    }

    @DeleteMapping("/{authorId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun unsubscribe(@PathVariable authorId: UUID) {
        service.unsubscribe(currentUser.id(), authorId)
    }

    @GetMapping
    fun mySubscriptions(): List<UUID> = service.authorsFollowedBy(currentUser.id())
}
