package org.maxizenit.maxigram.identity

import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.maxizenit.maxigram.support.AbstractIntegrationTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.UUID

@AutoConfigureMockMvc
class IdentitySecurityIT : AbstractIntegrationTest() {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun `me requires authentication`() {
        mockMvc.perform(get("/api/identity/me"))
            .andExpect(status().isUnauthorized())
    }

    @Test
    fun `me returns the account id from the jwt subject`() {
        val userId = UUID.randomUUID()

        mockMvc.perform(get("/api/identity/me").with(jwt().jwt { it.subject(userId.toString()) }))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", equalTo(userId.toString())))
    }
}
