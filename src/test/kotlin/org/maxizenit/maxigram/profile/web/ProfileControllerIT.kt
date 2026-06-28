package org.maxizenit.maxigram.profile.web

import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Test
import org.jooq.DSLContext
import org.maxizenit.maxigram.jooq.Tables.APP_USER
import org.maxizenit.maxigram.support.AbstractIntegrationTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.OffsetDateTime
import java.util.UUID

@AutoConfigureMockMvc
class ProfileControllerIT : AbstractIntegrationTest() {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var dsl: DSLContext

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

    private fun putProfile(userId: UUID, firstName: String) =
        mockMvc.perform(
            put("/api/profiles/me")
                .with(jwt().jwt { it.subject(userId.toString()) })
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """{"firstName":"$firstName","lastName":"Lee","birthdate":"2000-01-01","timezone":"Europe/Moscow","interestIds":[1,2]}""",
                ),
        )

    @Test
    fun `saves and returns the current user's profile`() {
        val userId = createUser()

        putProfile(userId, "Ann")
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.firstName", equalTo("Ann")))
            .andExpect(jsonPath("$.timezone", equalTo("Europe/Moscow")))
            .andExpect(jsonPath("$.interests", hasSize<Any>(2)))

        mockMvc.perform(get("/api/profiles/me").with(jwt().jwt { it.subject(userId.toString()) }))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.firstName", equalTo("Ann")))
    }

    @Test
    fun `keeps profiles isolated between users`() {
        val alice = createUser()
        val bob = createUser()

        putProfile(alice, "Ann").andExpect(status().isOk())
        putProfile(bob, "Bob").andExpect(status().isOk())

        mockMvc.perform(get("/api/profiles/$alice").with(jwt().jwt { it.subject(bob.toString()) }))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.firstName", equalTo("Ann")))
    }

    @Test
    fun `lists the seeded interests`() {
        val userId = createUser()

        mockMvc.perform(get("/api/interests").with(jwt().jwt { it.subject(userId.toString()) }))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize<Any>(5)))
            .andExpect(jsonPath("$[0].name", equalTo("Футбол")))
    }
}
