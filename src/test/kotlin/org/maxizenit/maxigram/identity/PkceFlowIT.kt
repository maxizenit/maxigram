package org.maxizenit.maxigram.identity

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.maxizenit.maxigram.support.AbstractIntegrationTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.mock.web.MockHttpSession
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import java.net.URI
import java.security.MessageDigest
import java.util.Base64

/**
 * Full Authorization Code + PKCE flow for the public SPA client, including the silent-renew
 * (prompt=none) path the frontend uses instead of refresh tokens (SAS does not issue them
 * to public clients by design).
 */
@AutoConfigureMockMvc
class PkceFlowIT : AbstractIntegrationTest() {

    @Autowired
    private lateinit var mockMvc: MockMvc

    private val verifier = "a".repeat(43)
    private val challenge: String =
        Base64.getUrlEncoder().withoutPadding()
            .encodeToString(MessageDigest.getInstance("SHA-256").digest(verifier.toByteArray()))

    // The authorize endpoint reads GET parameters from the query string, so queryParam() is required.
    private fun authorizeRequest(redirectUri: String, prompt: String? = null) =
        get("/oauth2/authorize")
            .queryParam("response_type", "code")
            .queryParam("client_id", "maxigram-spa")
            .queryParam("redirect_uri", redirectUri)
            .queryParam("scope", "openid profile email")
            .queryParam("code_challenge", challenge)
            .queryParam("code_challenge_method", "S256")
            .apply { prompt?.let { queryParam("prompt", it) } }

    private fun register(email: String) {
        mockMvc.perform(
            post("/api/identity/registrations")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"email":"$email","password":"supersecret"}"""),
        )
    }

    /** Logs in through the form and returns the authenticated session. */
    private fun login(email: String): MockHttpSession {
        val result =
            mockMvc.perform(
                post("/login").param("username", email).param("password", "supersecret").with(csrf()),
            ).andReturn()
        assertThat(result.response.redirectedUrl).doesNotContain("error")
        return result.request.getSession(false) as MockHttpSession
    }

    private fun obtainCode(session: MockHttpSession, redirectUri: String, prompt: String? = null): String {
        val result = mockMvc.perform(authorizeRequest(redirectUri, prompt).session(session)).andReturn()
        val location = result.response.getHeader("Location")
        assertThat(location)
            .describedAs(
                "authorize redirect (status=${result.response.status}, error=${result.response.errorMessage}, " +
                    "body=${result.response.contentAsString.take(300)})",
            )
            .startsWith(redirectUri)
            .contains("code=")
        return URI(location!!).query.split("&").first { it.startsWith("code=") }.removePrefix("code=")
    }

    @Test
    fun `full PKCE flow issues an access token with the configured TTL and no refresh token`() {
        register("pkce@example.com")
        val session = login("pkce@example.com")
        val code = obtainCode(session, "http://localhost:5173/callback")

        val token =
            mockMvc.perform(
                post("/oauth2/token")
                    .param("grant_type", "authorization_code")
                    .param("code", code)
                    .param("redirect_uri", "http://localhost:5173/callback")
                    .param("client_id", "maxigram-spa")
                    .param("code_verifier", verifier),
            ).andReturn().response.contentAsString

        assertThat(token).contains("\"access_token\"")
        // Configured TTL is 10 minutes (default would be ~300s); allow for issue-time skew.
        val expiresIn = Regex("\"expires_in\":(\\d+)").find(token)!!.groupValues[1].toInt()
        assertThat(expiresIn).isBetween(540, 600)
        assertThat(token).doesNotContain("refresh_token")
    }

    @Test
    fun `silent renew re-authorizes with prompt=none on the live session`() {
        register("renew@example.com")
        val session = login("renew@example.com")
        // The SPA's hidden-iframe renewal: same session, prompt=none, dedicated redirect uri.
        val code = obtainCode(session, "http://localhost:5173/silent-renew.html", prompt = "none")
        assertThat(code).isNotBlank()
    }

    @Test
    fun `silent renew without a session is sent to the login page (no code issued)`() {
        // The anonymous request never reaches the authorization endpoint: the SPA's silent
        // renew then times out and falls back to a full interactive re-login.
        val result =
            mockMvc.perform(authorizeRequest("http://localhost:5173/silent-renew.html", prompt = "none"))
                .andReturn()
        val location = result.response.getHeader("Location")
        assertThat(location).endsWith("/login")
        assertThat(location).doesNotContain("code=")
    }
}
