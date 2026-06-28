package org.maxizenit.maxigram.feed.web

import com.fasterxml.jackson.databind.ObjectMapper
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasSize
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.OffsetDateTime
import java.util.UUID

@AutoConfigureMockMvc
class FeedControllerIT : AbstractIntegrationTest() {

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

    private fun createPost(author: UUID, text: String): Long {
        val response =
            mockMvc.perform(
                post("/api/posts")
                    .with(jwt().jwt { it.subject(author.toString()) })
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"text":"$text"}"""),
            ).andExpect(status().isCreated()).andReturn().response.contentAsString
        return objectMapper.readTree(response).get("id").asLong()
    }

    @Test
    fun `feed shows posts from followed authors`() {
        val alice = createUser()
        val bob = createUser()
        mockMvc.perform(post("/api/subscriptions/$bob").with(jwt().jwt { it.subject(alice.toString()) }))
            .andExpect(status().isNoContent())

        createPost(bob, "hello from bob")

        mockMvc.perform(get("/api/feed").with(jwt().jwt { it.subject(alice.toString()) }))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize<Any>(1)))
            .andExpect(jsonPath("$[0].text", equalTo("hello from bob")))
            .andExpect(jsonPath("$[0].authorId", equalTo(bob.toString())))
    }

    @Test
    fun `comments can be added and listed`() {
        val author = createUser()
        val postId = createPost(author, "a post")

        mockMvc.perform(
            post("/api/posts/$postId/comments")
                .with(jwt().jwt { it.subject(author.toString()) })
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"text":"first comment"}"""),
        ).andExpect(status().isCreated())

        mockMvc.perform(get("/api/posts/$postId/comments").with(jwt().jwt { it.subject(author.toString()) }))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize<Any>(1)))
            .andExpect(jsonPath("$[0].text", equalTo("first comment")))
    }

    @Test
    fun `unknown post returns 404 and blank post returns 400`() {
        val user = createUser()
        val asUser = jwt().jwt { it.subject(user.toString()) }

        mockMvc.perform(get("/api/posts/999999").with(asUser)).andExpect(status().isNotFound())

        mockMvc.perform(
            post("/api/posts").with(asUser).contentType(MediaType.APPLICATION_JSON).content("""{"text":"  "}"""),
        ).andExpect(status().isBadRequest())
    }
}
