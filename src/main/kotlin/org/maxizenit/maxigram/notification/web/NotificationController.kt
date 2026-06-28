package org.maxizenit.maxigram.notification.web

import org.maxizenit.maxigram.common.CurrentUser
import org.maxizenit.maxigram.notification.Notification
import org.maxizenit.maxigram.notification.NotificationService
import org.maxizenit.maxigram.notification.NotificationType
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.time.Instant
import java.util.UUID

data class NotificationResponse(
    val id: Long,
    val type: NotificationType,
    val actorId: UUID?,
    val text: String,
    val read: Boolean,
    val createdAt: Instant,
)

private fun Notification.toResponse() = NotificationResponse(id, type, actorId, text, read, createdAt)

@RestController
@RequestMapping("/api/notifications")
class NotificationController(
    private val service: NotificationService,
    private val currentUser: CurrentUser,
) {

    @GetMapping
    fun list(): List<NotificationResponse> = service.listFor(currentUser.id()).map { it.toResponse() }

    @PostMapping("/{id}/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun markRead(@PathVariable id: Long) {
        service.markRead(id, currentUser.id())
    }
}
