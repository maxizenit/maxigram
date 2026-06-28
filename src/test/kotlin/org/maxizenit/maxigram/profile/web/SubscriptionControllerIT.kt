package org.maxizenit.maxigram.profile.web

import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasSize
import org.jooq.DSLContext
import org.junit.jupiter.api.Test
import org.maxizenit.maxigram.jooq.Tables.APP_USER
import org.maxizenit.maxigram.support.AbstractIntegrationTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.OffsetDateTime
import java.util.UUID

@AutoConfigureMockMvc
class SubscriptionControllerIT : AbstractIntegrationTest() {

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

    @Test
    fun `subscribe is idempotent and reflected in the list`() {
        val alice = createUser()
        val bob = createUser()
        val asAlice = jwt().jwt { it.subject(alice.toString()) }

        mockMvc.perform(post("/api/subscriptions/$bob").with(asAlice)).andExpect(status().isNoContent())
        mockMvc.perform(post("/api/subscriptions/$bob").with(asAlice)).andExpect(status().isNoContent())

        mockMvc.perform(get("/api/subscriptions").with(asAlice))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize<Any>(1)))
            .andExpect(jsonPath("$[0]", equalTo(bob.toString())))
    }

    @Test
    fun `unsubscribe removes the author`() {
        val alice = createUser()
        val bob = createUser()
        val asAlice = jwt().jwt { it.subject(alice.toString()) }

        mockMvc.perform(post("/api/subscriptions/$bob").with(asAlice)).andExpect(status().isNoContent())
        mockMvc.perform(delete("/api/subscriptions/$bob").with(asAlice)).andExpect(status().isNoContent())

        mockMvc.perform(get("/api/subscriptions").with(asAlice))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize<Any>(0)))
    }

    @Test
    fun `cannot subscribe to yourself`() {
        val alice = createUser()

        mockMvc.perform(post("/api/subscriptions/$alice").with(jwt().jwt { it.subject(alice.toString()) }))
            .andExpect(status().isBadRequest())
    }
}
