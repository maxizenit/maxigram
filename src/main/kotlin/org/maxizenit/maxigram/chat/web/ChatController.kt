package org.maxizenit.maxigram.chat.web

import org.maxizenit.maxigram.chat.Chat
import org.maxizenit.maxigram.chat.ChatService
import org.maxizenit.maxigram.chat.ChatView
import org.maxizenit.maxigram.common.CurrentUser
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant
import java.util.UUID

data class OpenChatRequest(val participantId: UUID)

/** Requester-relative view: in an anonymous chat the partner's id is masked (null). */
data class ChatResponse(
    val id: Long,
    val partnerId: UUID?,
    val anonymous: Boolean,
    val iAgreed: Boolean,
    val partnerAgreed: Boolean,
    val closed: Boolean,
    val newChatId: Long?,
    val createdAt: Instant,
)

data class ChatSummaryResponse(
    val id: Long,
    val partnerId: UUID?,
    val anonymous: Boolean,
    val lastMessage: String?,
    val createdAt: Instant,
)

private fun Chat.toResponse(requesterId: UUID): ChatResponse {
    val requesterIsFirst = firstParticipantId == requesterId
    val partner = if (requesterIsFirst) secondParticipantId else firstParticipantId
    return ChatResponse(
        id = id,
        partnerId = if (anonymous) null else partner,
        anonymous = anonymous,
        iAgreed = if (requesterIsFirst) firstAgreed else secondAgreed,
        partnerAgreed = if (requesterIsFirst) secondAgreed else firstAgreed,
        closed = closed,
        newChatId = newChatId,
        createdAt = createdAt,
    )
}

private fun ChatView.toResponse(requesterId: UUID): ChatSummaryResponse {
    val partner = if (firstParticipantId == requesterId) secondParticipantId else firstParticipantId
    return ChatSummaryResponse(id, if (anonymous) null else partner, anonymous, lastMessage, createdAt)
}

@RestController
@RequestMapping("/api/chats")
class ChatController(
    private val service: ChatService,
    private val currentUser: CurrentUser,
) {

    @PostMapping
    fun open(@RequestBody request: OpenChatRequest): ChatResponse =
        service.openChatWith(currentUser.id(), request.participantId).toResponse(currentUser.id())

    @GetMapping
    fun myChats(): List<ChatSummaryResponse> {
        val me = currentUser.id()
        return service.chatsOf(me).map { it.toResponse(me) }
    }

    @GetMapping("/{id}")
    fun byId(@PathVariable id: Long): ChatResponse =
        service.requireParticipant(id, currentUser.id()).toResponse(currentUser.id())

    @PostMapping("/{id}/agreement")
    fun agree(@PathVariable id: Long): ChatResponse =
        service.agreeToDeAnonymization(id, currentUser.id()).toResponse(currentUser.id())

    @PostMapping("/{id}/close")
    fun close(@PathVariable id: Long): ChatResponse =
        service.closeAnonymousChat(id, currentUser.id()).toResponse(currentUser.id())
}
