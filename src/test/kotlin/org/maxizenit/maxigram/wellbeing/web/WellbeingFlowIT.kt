package org.maxizenit.maxigram.wellbeing.web

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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.Instant
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID

@AutoConfigureMockMvc
class WellbeingFlowIT : AbstractIntegrationTest() {

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

    private fun setRestraint(userId: UUID, start: Instant, end: Instant) =
        mockMvc.perform(
            put("/api/wellbeing/restraint")
                .with(jwt().jwt { it.subject(userId.toString()) })
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"startTime":"$start","endTime":"$end"}"""),
        )

    @Test
    fun `an active restraint blocks the app but not the wellbeing endpoints`() {
        val user = createUser()
        val asUser = jwt().jwt { it.subject(user.toString()) }
        val now = Instant.now()

        setRestraint(user, now.minus(1, ChronoUnit.HOURS), now.plus(1, ChronoUnit.HOURS))
            .andExpect(status().isOk())

        mockMvc.perform(get("/api/feed").with(asUser)).andExpect(status().isLocked())
        mockMvc.perform(get("/api/wellbeing/restraint").with(asUser)).andExpect(status().isOk())
        mockMvc.perform(delete("/api/wellbeing/restraint").with(asUser)).andExpect(status().isLocked())
    }

    @Test
    fun `a future restraint does not block and can be removed`() {
        val user = createUser()
        val asUser = jwt().jwt { it.subject(user.toString()) }
        val now = Instant.now()

        setRestraint(user, now.plus(1, ChronoUnit.HOURS), now.plus(2, ChronoUnit.HOURS))
            .andExpect(status().isOk())

        mockMvc.perform(get("/api/feed").with(asUser)).andExpect(status().isOk())
        mockMvc.perform(delete("/api/wellbeing/restraint").with(asUser)).andExpect(status().isNoContent())
    }
}
