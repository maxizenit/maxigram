package org.maxizenit.maxigram.identity.web

import org.assertj.core.api.Assertions.assertThat
import org.jooq.DSLContext
import org.junit.jupiter.api.Test
import org.maxizenit.jooq.Tables.APP_USER
import org.maxizenit.maxigram.support.AbstractIntegrationTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post

@AutoConfigureMockMvc
class RegistrationControllerIT : AbstractIntegrationTest() {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var dsl: DSLContext

    @Test
    fun `registers a user and persists it unverified`() {
        mockMvc.post("/api/identity/registrations") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"email":"alice@example.com","password":"supersecret"}"""
        }.andExpect {
            status { isCreated() }
            jsonPath("$.email") { value("alice@example.com") }
            jsonPath("$.id") { exists() }
        }

        val verified =
            dsl.select(APP_USER.EMAIL_VERIFIED)
                .from(APP_USER)
                .where(APP_USER.EMAIL.eq("alice@example.com"))
                .fetchOne(APP_USER.EMAIL_VERIFIED)
        assertThat(verified).isFalse()
    }

    @Test
    fun `rejects a duplicate email with 409`() {
        val body = """{"email":"bob@example.com","password":"supersecret"}"""
        mockMvc.post("/api/identity/registrations") {
            contentType = MediaType.APPLICATION_JSON
            content = body
        }.andExpect { status { isCreated() } }

        mockMvc.post("/api/identity/registrations") {
            contentType = MediaType.APPLICATION_JSON
            content = body
        }.andExpect { status { isConflict() } }
    }

    @Test
    fun `rejects a weak password with 400`() {
        mockMvc.post("/api/identity/registrations") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"email":"weak@example.com","password":"short"}"""
        }.andExpect { status { isBadRequest() } }
    }
}
