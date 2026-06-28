package org.maxizenit.maxigram.identity

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID

class PasswordResetServiceTest {

    private val users = InMemoryUserAccountRepository()
    private val tokens = InMemoryOneTimeTokenRepository()
    private val emailSender = RecordingEmailSender()
    private val encoder = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8()
    private val clock = Clock.fixed(Instant.parse("2026-06-28T12:00:00Z"), ZoneOffset.UTC)
    private val service =
        PasswordResetService(users, OneTimeTokenService(tokens, clock), emailSender, encoder)

    private val user = users.insert(UUID.randomUUID(), "user@example.com", "old-hash", Instant.now(clock))

    @Test
    fun `requesting a reset for a known email issues a token and sends an email`() {
        service.requestReset("User@Example.com")

        assertThat(tokens.tokens).hasSize(1)
        assertThat(emailSender.last().to).isEqualTo("user@example.com")
    }

    @Test
    fun `requesting a reset for an unknown email does nothing`() {
        service.requestReset("nobody@example.com")

        assertThat(tokens.tokens).isEmpty()
        assertThat(emailSender.sent).isEmpty()
    }

    @Test
    fun `resetting with a valid token updates the password hash`() {
        service.requestReset("user@example.com")
        val token = tokens.tokens.keys.single()

        service.reset(token, "brand-new-password")

        val newHash = users.passwordHashOf(user.id)!!
        assertThat(newHash).isNotEqualTo("old-hash")
        assertThat(encoder.matches("brand-new-password", newHash)).isTrue()
    }

    @Test
    fun `rejects a weak new password without consuming the token`() {
        service.requestReset("user@example.com")
        val token = tokens.tokens.keys.single()

        assertThatThrownBy { service.reset(token, "short") }
            .isInstanceOf(WeakPasswordException::class.java)

        assertThat(tokens.find(token)!!.consumedAt).isNull()
        assertThat(users.passwordHashOf(user.id)).isEqualTo("old-hash")
    }

    @Test
    fun `rejects an unknown reset token`() {
        assertThatThrownBy { service.reset(UUID.randomUUID(), "brand-new-password") }
            .isInstanceOf(InvalidTokenException::class.java)
    }
}
