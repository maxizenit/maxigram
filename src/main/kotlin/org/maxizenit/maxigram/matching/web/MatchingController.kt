package org.maxizenit.maxigram.matching.web

import org.maxizenit.maxigram.common.CurrentUser
import org.maxizenit.maxigram.matching.MatchResult
import org.maxizenit.maxigram.matching.MatchingService
import org.maxizenit.maxigram.matching.ProfileRequiredException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

data class MatchResponse(val matched: Boolean, val chatId: Long?)

@RestController
@RequestMapping("/api/matching/requests")
class MatchingController(
    private val service: MatchingService,
    private val currentUser: CurrentUser,
) {

    @PostMapping
    fun request(): MatchResponse =
        when (val result = service.requestMatch(currentUser.id())) {
            is MatchResult.Matched -> MatchResponse(true, result.chatId)
            MatchResult.Queued -> MatchResponse(false, null)
        }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun leave() {
        service.leaveQueue(currentUser.id())
    }

    @ExceptionHandler(ProfileRequiredException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleProfileRequired(e: ProfileRequiredException): Map<String, String?> =
        mapOf("error" to e.message)
}
