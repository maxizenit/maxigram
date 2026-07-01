package org.maxizenit.maxigram.profile.web

import org.maxizenit.maxigram.common.CurrentUser
import org.maxizenit.maxigram.profile.ProfileNotFoundException
import org.maxizenit.maxigram.profile.ProfileService
import org.maxizenit.maxigram.profile.UserProfile
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.util.UUID

data class ProfileRequest(
    val firstName: String,
    val lastName: String,
    val birthdate: LocalDate,
    val timezone: String = "UTC",
    val interestIds: List<Long> = emptyList(),
)

data class InterestResponse(val id: Long, val name: String)

data class ProfileResponse(
    val id: UUID,
    val firstName: String,
    val lastName: String,
    val birthdate: LocalDate,
    val timezone: String,
    val interests: List<InterestResponse>,
)

private fun UserProfile.toResponse() =
    ProfileResponse(id, firstName, lastName, birthdate, timezone.id, interests.map { InterestResponse(it.id, it.name) })

@RestController
@RequestMapping("/api/profiles")
class ProfileController(
    private val service: ProfileService,
    private val currentUser: CurrentUser,
) {

    @PutMapping("/me")
    fun saveMine(@RequestBody request: ProfileRequest): ProfileResponse =
        service.save(
            currentUser.id(),
            request.firstName,
            request.lastName,
            request.birthdate,
            request.timezone,
            request.interestIds,
        ).toResponse()

    @GetMapping("/me")
    fun mine(): ProfileResponse {
        val id = currentUser.id()
        return service.find(id)?.toResponse() ?: throw ProfileNotFoundException(id)
    }

    @GetMapping("/{userId}")
    fun byId(@PathVariable userId: UUID): ProfileResponse =
        service.find(userId)?.toResponse() ?: throw ProfileNotFoundException(userId)

    /** List view: `query` searches by name, `ids` resolves a batch (for chat lists etc.). */
    @GetMapping
    fun list(
        @RequestParam(required = false) query: String?,
        @RequestParam(required = false) ids: List<UUID>?,
    ): List<ProfileResponse> =
        when {
            !query.isNullOrBlank() -> service.search(query).map { it.toResponse() }
            !ids.isNullOrEmpty() -> service.findAllByIds(ids).map { it.toResponse() }
            else -> emptyList()
        }
}
