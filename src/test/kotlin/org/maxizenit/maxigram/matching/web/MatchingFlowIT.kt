package org.maxizenit.maxigram.matching.web

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.nullValue
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.OffsetDateTime
import java.util.UUID

@AutoConfigureMockMvc
class MatchingFlowIT : AbstractIntegrationTest() {

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

    private fun createProfile(userId: UUID) {
        mockMvc.perform(
            put("/api/profiles/me")
                .with(jwt().jwt { it.subject(userId.toString()) })
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"firstName":"A","lastName":"B","birthdate":"2000-01-01","timezone":"UTC","interestIds":[1,2]}"""),
        ).andExpect(status().isOk())
    }

    private fun requestMatch(userId: UUID): JsonNode {
        val body =
            mockMvc.perform(post("/api/matching/requests").with(jwt().jwt { it.subject(userId.toString()) }))
                .andExpect(status().isOk())
                .andReturn().response.contentAsString
        return objectMapper.readTree(body)
    }

    @Test
    fun `two similar users are matched into an anonymous chat and notified`() {
        val alice = createUser()
        val bob = createUser()
        createProfile(alice)
        createProfile(bob)

        assertThat(requestMatch(alice).get("matched").asBoolean()).isFalse()

        val bobResult = requestMatch(bob)
        assertThat(bobResult.get("matched").asBoolean()).isTrue()
        val chatId = bobResult.get("chatId").asLong()

        mockMvc.perform(get("/api/chats/$chatId").with(jwt().jwt { it.subject(alice.toString()) }))
            .andExpect(jsonPath("$.anonymous", equalTo(true)))
            .andExpect(jsonPath("$.partnerId", nullValue()))

        mockMvc.perform(get("/api/notifications").with(jwt().jwt { it.subject(alice.toString()) }))
            .andExpect(jsonPath("$[0].type", equalTo("MATCHED")))
    }

    @Test
    fun `requesting a match without a profile is rejected`() {
        val user = createUser()

        mockMvc.perform(post("/api/matching/requests").with(jwt().jwt { it.subject(user.toString()) }))
            .andExpect(status().isBadRequest())
    }
}
