package org.maxizenit.maxigram.chat.web

import org.maxizenit.maxigram.chat.Message
import org.maxizenit.maxigram.chat.MessageService
import org.maxizenit.maxigram.common.CurrentUser
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.time.Instant
import java.util.UUID

data class MessageRequest(val text: String)

data class MessageResponse(
    val id: Long,
    val chatId: Long,
    val senderId: UUID,
    val text: String,
    val createdAt: Instant,
    val read: Boolean,
)

fun Message.toResponse() = MessageResponse(id, chatId, senderId, text, createdAt, read)

@RestController
@RequestMapping("/api/chats/{chatId}/messages")
class MessageController(
    private val service: MessageService,
    private val currentUser: CurrentUser,
) {

    @GetMapping
    fun conversation(@PathVariable chatId: Long): List<MessageResponse> =
        service.readConversation(chatId, currentUser.id()).map { it.toResponse() }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun send(@PathVariable chatId: Long, @RequestBody request: MessageRequest): MessageResponse =
        service.send(chatId, currentUser.id(), request.text).toResponse()
}
