package org.maxizenit.maxigram.feed.web

import com.fasterxml.jackson.databind.ObjectMapper
import org.hamcrest.Matchers.equalTo
import org.jooq.DSLContext
import org.junit.jupiter.api.Test
import org.maxizenit.maxigram.jooq.Tables.APP_USER
import org.maxizenit.maxigram.support.AbstractIntegrationTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
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
class LikeControllerIT : AbstractIntegrationTest() {

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

    private fun createPost(author: UUID, text: String): Long = createdId(
        post("/api/posts")
            .with(jwt().jwt { it.subject(author.toString()) })
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"text":"$text"}"""),
    )

    private fun createComment(postId: Long, author: UUID, text: String): Long = createdId(
        post("/api/posts/$postId/comments")
            .with(jwt().jwt { it.subject(author.toString()) })
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"text":"$text"}"""),
    )

    private fun createdId(builder: org.springframework.test.web.servlet.RequestBuilder): Long {
        val body = mockMvc.perform(builder).andExpect(status().isCreated()).andReturn().response.contentAsString
        return objectMapper.readTree(body).get("id").asLong()
    }

    @Test
    fun `liking a post is idempotent and reflected in counts and like-state`() {
        val author = createUser()
        val liker = createUser()
        val postId = createPost(author, "likeable")
        val asLiker = jwt().jwt { it.subject(liker.toString()) }

        mockMvc.perform(post("/api/posts/$postId/likes").with(asLiker)).andExpect(status().isNoContent())
        mockMvc.perform(post("/api/posts/$postId/likes").with(asLiker)).andExpect(status().isNoContent())

        mockMvc.perform(get("/api/posts/$postId").with(asLiker))
            .andExpect(jsonPath("$.likesCount", equalTo(1)))
            .andExpect(jsonPath("$.likedByMe", equalTo(true)))

        mockMvc.perform(get("/api/posts/$postId").with(jwt().jwt { it.subject(author.toString()) }))
            .andExpect(jsonPath("$.likesCount", equalTo(1)))
            .andExpect(jsonPath("$.likedByMe", equalTo(false)))

        mockMvc.perform(delete("/api/posts/$postId/likes").with(asLiker)).andExpect(status().isNoContent())
        mockMvc.perform(get("/api/posts/$postId").with(asLiker))
            .andExpect(jsonPath("$.likesCount", equalTo(0)))
            .andExpect(jsonPath("$.likedByMe", equalTo(false)))
    }

    @Test
    fun `comment likes are counted per requester`() {
        val author = createUser()
        val liker = createUser()
        val postId = createPost(author, "post")
        val commentId = createComment(postId, author, "comment")

        mockMvc.perform(post("/api/comments/$commentId/likes").with(jwt().jwt { it.subject(liker.toString()) }))
            .andExpect(status().isNoContent())

        mockMvc.perform(get("/api/posts/$postId/comments").with(jwt().jwt { it.subject(liker.toString()) }))
            .andExpect(jsonPath("$[0].likesCount", equalTo(1)))
            .andExpect(jsonPath("$[0].likedByMe", equalTo(true)))
    }

    @Test
    fun `feed reports correct counters for every post`() {
        val alice = createUser()
        val bob = createUser()
        mockMvc.perform(post("/api/subscriptions/$bob").with(jwt().jwt { it.subject(alice.toString()) }))
            .andExpect(status().isNoContent())

        val first = createPost(bob, "first")
        createPost(bob, "second")
        val asAlice = jwt().jwt { it.subject(alice.toString()) }
        mockMvc.perform(post("/api/posts/$first/likes").with(asAlice)).andExpect(status().isNoContent())
        createComment(first, alice, "nice")

        // Newest first: [second, first]. The aggregate query returns all counters in one round-trip.
        mockMvc.perform(get("/api/feed").with(asAlice))
            .andExpect(jsonPath("$[0].likesCount", equalTo(0)))
            .andExpect(jsonPath("$[0].commentsCount", equalTo(0)))
            .andExpect(jsonPath("$[1].likesCount", equalTo(1)))
            .andExpect(jsonPath("$[1].commentsCount", equalTo(1)))
            .andExpect(jsonPath("$[1].likedByMe", equalTo(true)))
    }

    @Test
    fun `liking a missing post returns 404`() {
        val user = createUser()

        mockMvc.perform(post("/api/posts/999999/likes").with(jwt().jwt { it.subject(user.toString()) }))
            .andExpect(status().isNotFound())
    }
}
