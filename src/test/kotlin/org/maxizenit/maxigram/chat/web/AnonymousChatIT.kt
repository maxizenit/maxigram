package org.maxizenit.maxigram.chat.web

import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.nullValue
import org.jooq.DSLContext
import org.junit.jupiter.api.Test
import org.maxizenit.maxigram.chat.ChatService
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
class AnonymousChatIT : AbstractIntegrationTest() {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var dsl: DSLContext

    @Autowired
    private lateinit var chatService: ChatService

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

    private fun send(chatId: Long, sender: UUID, text: String) {
        mockMvc.perform(
            post("/api/chats/$chatId/messages")
                .with(jwt().jwt { it.subject(sender.toString()) })
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"text":"$text"}"""),
        ).andExpect(status().isCreated())
    }

    @Test
    fun `anonymous chat masks the partner and the partner's messages`() {
        val alice = createUser()
        val bob = createUser()
        val chat = chatService.createAnonymousChat(alice, bob)

        mockMvc.perform(get("/api/chats/${chat.id}").with(jwt().jwt { it.subject(alice.toString()) }))
            .andExpect(jsonPath("$.anonymous", equalTo(true)))
            .andExpect(jsonPath("$.partnerId", nullValue()))

        send(chat.id, alice, "from alice")
        send(chat.id, bob, "from bob")

        mockMvc.perform(get("/api/chats/${chat.id}/messages").with(jwt().jwt { it.subject(alice.toString()) }))
            .andExpect(jsonPath("$[0].senderId", equalTo(alice.toString())))
            .andExpect(jsonPath("$[1].senderId", nullValue()))
            .andExpect(jsonPath("$[1].text", equalTo("from bob")))
    }

    @Test
    fun `mutual agreement converts the chat in place and unmasks the history`() {
        val alice = createUser()
        val bob = createUser()
        val chat = chatService.createAnonymousChat(alice, bob)
        send(chat.id, alice, "from alice")
        send(chat.id, bob, "from bob")

        mockMvc.perform(post("/api/chats/${chat.id}/agreement").with(jwt().jwt { it.subject(alice.toString()) }))
            .andExpect(jsonPath("$.iAgreed", equalTo(true)))
            .andExpect(jsonPath("$.anonymous", equalTo(true)))

        // The second consent converts the same chat: identities revealed, thread preserved.
        mockMvc.perform(post("/api/chats/${chat.id}/agreement").with(jwt().jwt { it.subject(bob.toString()) }))
            .andExpect(jsonPath("$.anonymous", equalTo(false)))
            .andExpect(jsonPath("$.partnerId", equalTo(alice.toString())))

        mockMvc.perform(get("/api/chats/${chat.id}").with(jwt().jwt { it.subject(alice.toString()) }))
            .andExpect(jsonPath("$.anonymous", equalTo(false)))
            .andExpect(jsonPath("$.partnerId", equalTo(bob.toString())))

        // History is retroactively unmasked -- safe in a 1-on-1 chat once identities are known.
        mockMvc.perform(get("/api/chats/${chat.id}/messages").with(jwt().jwt { it.subject(alice.toString()) }))
            .andExpect(jsonPath("$[0].senderId", equalTo(alice.toString())))
            .andExpect(jsonPath("$[1].senderId", equalTo(bob.toString())))
    }

    @Test
    fun `closing an anonymous chat blocks further messages`() {
        val alice = createUser()
        val bob = createUser()
        val chat = chatService.createAnonymousChat(alice, bob)

        mockMvc.perform(post("/api/chats/${chat.id}/close").with(jwt().jwt { it.subject(alice.toString()) }))
            .andExpect(jsonPath("$.closed", equalTo(true)))

        mockMvc.perform(
            post("/api/chats/${chat.id}/messages")
                .with(jwt().jwt { it.subject(alice.toString()) })
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"text":"still here?"}"""),
        ).andExpect(status().isBadRequest())
    }
}
