package org.maxizenit.maxigram.chat.web

import com.fasterxml.jackson.databind.ObjectMapper
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasSize
import org.jooq.DSLContext
import org.junit.jupiter.api.Test
import org.maxizenit.maxigram.jooq.Tables.APP_USER
import org.maxizenit.maxigram.support.AbstractIntegrationTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.OffsetDateTime
import java.util.UUID

@AutoConfigureMockMvc
class MessageFlowIT : AbstractIntegrationTest() {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var dsl: DSLContext

    private val objectMapper = ObjectMapper()

    private fun createUser(): UUID {
        val id = UUID.randomUUID()
        dsl.insertInto(APP_USER)
            .set(APP_USER.ID, id)
            .set(APP_USER.EMAIL, "$id@example.com")
            .set(APP_USER.PASSWORD_HASH, "x")
            .set(APP_USER.EMAIL_VERIFIED, true)
            .set(APP_USER.CREATED_AT, OffsetDateTime.now())
            .execute()
        return id
    }

    private fun openChat(requester: UUID, other: UUID): Long {
        val body =
            mockMvc.perform(
                post("/api/chats")
                    .with(jwt().jwt { it.subject(requester.toString()) })
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"participantId":"$other"}"""),
            ).andExpect(status().isOk()).andReturn().response.contentAsString
        return objectMapper.readTree(body).get("id").asLong()
    }

    private fun sendMessage(chatId: Long, sender: UUID, text: String) {
        mockMvc.perform(
            post("/api/chats/$chatId/messages")
                .with(jwt().jwt { it.subject(sender.toString()) })
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"text":"$text"}"""),
        ).andExpect(status().isCreated())
    }

    @Test
    fun `messages flow and the chat list shows the most recent message`() {
        val alice = createUser()
        val bob = createUser()
        val chatId = openChat(alice, bob)

        sendMessage(chatId, alice, "first")
        sendMessage(chatId, alice, "second")

        // Bob reads: the other party's messages become read; conversation is ordered oldest-first.
        mockMvc.perform(get("/api/chats/$chatId/messages").with(jwt().jwt { it.subject(bob.toString()) }))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize<Any>(2)))
            .andExpect(jsonPath("$[0].text", equalTo("first")))
            .andExpect(jsonPath("$[0].read", equalTo(true)))

        // Chat list shows the latest message (v1 returned the first message here).
        mockMvc.perform(get("/api/chats").with(jwt().jwt { it.subject(bob.toString()) }))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].lastMessage", equalTo("second")))
    }

    @Test
    fun `a non-participant cannot access the chat`() {
        val alice = createUser()
        val bob = createUser()
        val stranger = createUser()
        val chatId = openChat(alice, bob)

        mockMvc.perform(get("/api/chats/$chatId").with(jwt().jwt { it.subject(stranger.toString()) }))
            .andExpect(status().isForbidden())
        mockMvc.perform(get("/api/chats/$chatId/messages").with(jwt().jwt { it.subject(stranger.toString()) }))
            .andExpect(status().isForbidden())
    }

    @Test
    fun `cannot open a chat with yourself`() {
        val alice = createUser()

        mockMvc.perform(
            post("/api/chats")
                .with(jwt().jwt { it.subject(alice.toString()) })
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"participantId":"$alice"}"""),
        ).andExpect(status().isBadRequest())
    }
}
