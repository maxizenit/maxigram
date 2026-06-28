package org.maxizenit.maxigram.wellbeing.web

import org.maxizenit.maxigram.common.CurrentUser
import org.maxizenit.maxigram.wellbeing.SelfRestraint
import org.maxizenit.maxigram.wellbeing.WellbeingService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

data class RestraintRequest(val startTime: Instant, val endTime: Instant)

data class RestraintResponse(val startTime: Instant, val endTime: Instant)

private fun SelfRestraint.toResponse() = RestraintResponse(startTime, endTime)

@RestController
@RequestMapping("/api/wellbeing/restraint")
class WellbeingController(
    private val service: WellbeingService,
    private val currentUser: CurrentUser,
) {

    @GetMapping
    fun current(): RestraintResponse? = service.getRestraint(currentUser.id())?.toResponse()

    @PutMapping
    fun set(@RequestBody request: RestraintRequest): RestraintResponse =
        service.setRestraint(currentUser.id(), request.startTime, request.endTime).toResponse()

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun remove() {
        service.removeRestraint(currentUser.id())
    }
}
