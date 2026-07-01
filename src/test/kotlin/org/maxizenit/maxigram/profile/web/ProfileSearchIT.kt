package org.maxizenit.maxigram.profile.web

import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasSize
import org.jooq.DSLContext
import org.junit.jupiter.api.Test
import org.maxizenit.maxigram.jooq.Tables.APP_USER
import org.maxizenit.maxigram.profile.ProfileService
import org.maxizenit.maxigram.support.AbstractIntegrationTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

@AutoConfigureMockMvc
class ProfileSearchIT : AbstractIntegrationTest() {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var dsl: DSLContext

    @Autowired
    private lateinit var profileService: ProfileService

    private fun createUserWithProfile(firstName: String, lastName: String): UUID {
        val id = UUID.randomUUID()
        dsl.insertInto(APP_USER)
            .set(APP_USER.ID, id)
            .set(APP_USER.EMAIL, "$id@example.com")
            .set(APP_USER.PASSWORD_HASH, "x")
            .set(APP_USER.EMAIL_VERIFIED, true)
            .set(APP_USER.CREATED_AT, OffsetDateTime.now())
            .execute()
        profileService.save(id, firstName, lastName, LocalDate.of(2000, 1, 1), "UTC", emptyList())
        return id
    }

    @Test
    fun `searches profiles by name substring, case-insensitively`() {
        val viewer = createUserWithProfile("Смотрящий", "Тестов")
        createUserWithProfile("Ярополк", "Уникальнов")
        createUserWithProfile("Ярослава", "Другая")

        mockMvc.perform(
            get("/api/profiles").queryParam("query", "яро").with(jwt().jwt { it.subject(viewer.toString()) }),
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize<Any>(2)))
            .andExpect(jsonPath("$[0].firstName", equalTo("Ярополк")))
            .andExpect(jsonPath("$[1].firstName", equalTo("Ярослава")))
    }

    @Test
    fun `resolves a batch of profiles by ids`() {
        val viewer = createUserWithProfile("Зритель", "Батчев")
        val first = createUserWithProfile("Один", "Первый")
        val second = createUserWithProfile("Два", "Второй")

        mockMvc.perform(
            get("/api/profiles")
                .queryParam("ids", "$first,$second")
                .with(jwt().jwt { it.subject(viewer.toString()) }),
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize<Any>(2)))
    }

    @Test
    fun `no parameters means an empty list, not an error`() {
        val viewer = createUserWithProfile("Пустой", "Запросов")
        mockMvc.perform(get("/api/profiles").with(jwt().jwt { it.subject(viewer.toString()) }))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize<Any>(0)))
    }
}
