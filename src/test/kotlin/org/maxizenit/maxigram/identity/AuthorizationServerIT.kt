package org.maxizenit.maxigram.identity

import org.junit.jupiter.api.Test
import org.maxizenit.maxigram.support.AbstractIntegrationTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@AutoConfigureMockMvc
class AuthorizationServerIT : AbstractIntegrationTest() {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun `exposes the openid provider configuration`() {
        mockMvc.perform(get("/.well-known/openid-configuration"))
            .andExpect(status().isOk())
    }

    @Test
    fun `exposes the jwk set`() {
        mockMvc.perform(get("/oauth2/jwks"))
            .andExpect(status().isOk())
    }
}
