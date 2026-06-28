package org.maxizenit.maxigram.notification.web

import com.fasterxml.jackson.databind.ObjectMapper
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.OffsetDateTime
import java.util.UUID

@AutoConfigureMockMvc
class NotificationFlowIT : AbstractIntegrationTest() {

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

    @Test
    fun `subscribing notifies the author, who can read and dismiss it`() {
        val alice = createUser()
        val bob = createUser()

        mockMvc.perform(post("/api/subscriptions/$bob").with(jwt().jwt { it.subject(alice.toString()) }))
            .andExpect(status().isNoContent())

        val listJson =
            mockMvc.perform(get("/api/notifications").with(jwt().jwt { it.subject(bob.toString()) }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize<Any>(1)))
                .andExpect(jsonPath("$[0].type", equalTo("NEW_SUBSCRIBER")))
                .andExpect(jsonPath("$[0].actorId", equalTo(alice.toString())))
                .andExpect(jsonPath("$[0].read", equalTo(false)))
                .andReturn().response.contentAsString
        val notificationId = objectMapper.readTree(listJson).get(0).get("id").asLong()

        mockMvc.perform(post("/api/notifications/$notificationId/read").with(jwt().jwt { it.subject(bob.toString()) }))
            .andExpect(status().isNoContent())

        mockMvc.perform(get("/api/notifications").with(jwt().jwt { it.subject(bob.toString()) }))
            .andExpect(jsonPath("$[0].read", equalTo(true)))
    }
}
