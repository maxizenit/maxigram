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

data class ChatResponse(
    val id: Long,
    val firstParticipantId: UUID,
    val secondParticipantId: UUID,
    val createdAt: Instant,
)

data class ChatSummaryResponse(
    val id: Long,
    val firstParticipantId: UUID,
    val secondParticipantId: UUID,
    val createdAt: Instant,
    val lastMessage: String?,
)

private fun Chat.toResponse() = ChatResponse(id, firstParticipantId, secondParticipantId, createdAt)

private fun ChatView.toResponse() =
    ChatSummaryResponse(id, firstParticipantId, secondParticipantId, createdAt, lastMessage)

@RestController
@RequestMapping("/api/chats")
class ChatController(
    private val service: ChatService,
    private val currentUser: CurrentUser,
) {

    @PostMapping
    fun open(@RequestBody request: OpenChatRequest): ChatResponse =
        service.openChatWith(currentUser.id(), request.participantId).toResponse()

    @GetMapping
    fun myChats(): List<ChatSummaryResponse> = service.chatsOf(currentUser.id()).map { it.toResponse() }

    @GetMapping("/{id}")
    fun byId(@PathVariable id: Long): ChatResponse =
        service.requireParticipant(id, currentUser.id()).toResponse()
}
