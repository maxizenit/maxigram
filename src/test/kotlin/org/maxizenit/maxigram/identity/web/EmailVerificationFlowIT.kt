package org.maxizenit.maxigram.identity.web

import org.assertj.core.api.Assertions.assertThat
import org.jooq.DSLContext
import org.junit.jupiter.api.Test
import org.maxizenit.maxigram.jooq.Tables.APP_USER
import org.maxizenit.maxigram.jooq.Tables.ONE_TIME_TOKEN
import org.maxizenit.maxigram.support.AbstractIntegrationTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@AutoConfigureMockMvc
class EmailVerificationFlowIT : AbstractIntegrationTest() {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var dsl: DSLContext

    @Test
    fun `registration issues a verification token that verifies the account`() {
        val email = "verify-me@example.com"

        mockMvc.perform(
            post("/api/identity/registrations")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"email":"$email","password":"supersecret"}"""),
        ).andExpect(status().isCreated())

        val userId =
            dsl.select(APP_USER.ID).from(APP_USER).where(APP_USER.EMAIL.eq(email)).fetchOne(APP_USER.ID)!!
        val token =
            dsl.select(ONE_TIME_TOKEN.TOKEN)
                .from(ONE_TIME_TOKEN)
                .where(ONE_TIME_TOKEN.USER_ID.eq(userId).and(ONE_TIME_TOKEN.PURPOSE.eq("EMAIL_VERIFICATION")))
                .fetchOne(ONE_TIME_TOKEN.TOKEN)!!

        mockMvc.perform(post("/api/identity/email-verifications/$token"))
            .andExpect(status().isNoContent())

        val verified =
            dsl.select(APP_USER.EMAIL_VERIFIED)
                .from(APP_USER)
                .where(APP_USER.ID.eq(userId))
                .fetchOne(APP_USER.EMAIL_VERIFIED)
        assertThat(verified).isTrue()
    }
}
