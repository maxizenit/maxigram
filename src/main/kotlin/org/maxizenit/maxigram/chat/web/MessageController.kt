package org.maxizenit.maxigram.chat.web

import org.maxizenit.maxigram.chat.ChatService
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

/** `senderId` is null for the partner's messages in an anonymous chat. */
data class MessageResponse(
    val id: Long,
    val chatId: Long,
    val senderId: UUID?,
    val text: String,
    val createdAt: Instant,
    val read: Boolean,
)

fun Message.toResponse() = MessageResponse(id, chatId, senderId, text, createdAt, read)

/** Masks the sender for the viewer: in an anonymous chat, the other party's id is hidden. */
fun Message.toMaskedResponse(viewerId: UUID, anonymous: Boolean): MessageResponse =
    MessageResponse(id, chatId, if (anonymous && senderId != viewerId) null else senderId, text, createdAt, read)

/** Masks the sender for a shared broadcast: hidden for everyone in an anonymous chat. */
fun Message.toBroadcastResponse(anonymous: Boolean): MessageResponse =
    MessageResponse(id, chatId, if (anonymous) null else senderId, text, createdAt, read)

@RestController
@RequestMapping("/api/chats/{chatId}/messages")
class MessageController(
    private val messageService: MessageService,
    private val chatService: ChatService,
    private val currentUser: CurrentUser,
) {

    @GetMapping
    fun conversation(@PathVariable chatId: Long): List<MessageResponse> {
        val me = currentUser.id()
        val chat = chatService.requireParticipant(chatId, me)
        return messageService.readConversation(chatId, me).map { it.toMaskedResponse(me, chat.anonymous) }
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun send(@PathVariable chatId: Long, @RequestBody request: MessageRequest): MessageResponse =
        messageService.send(chatId, currentUser.id(), request.text).toResponse()
}
