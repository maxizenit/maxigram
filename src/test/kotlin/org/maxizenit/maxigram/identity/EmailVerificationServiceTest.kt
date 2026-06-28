package org.maxizenit.maxigram.identity

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID

class EmailVerificationServiceTest {

    private val users = InMemoryUserAccountRepository()
    private val tokens = InMemoryOneTimeTokenRepository()
    private val emailSender = RecordingEmailSender()
    private val clock = Clock.fixed(Instant.parse("2026-06-28T12:00:00Z"), ZoneOffset.UTC)
    private val service = EmailVerificationService(users, OneTimeTokenService(tokens, clock), emailSender)

    private val user = users.insert(UUID.randomUUID(), "user@example.com", "hash", Instant.now(clock))

    @Test
    fun `sends a verification email with a token`() {
        service.sendVerificationEmail(user.id, user.email)

        assertThat(tokens.tokens).hasSize(1)
        assertThat(emailSender.last().to).isEqualTo("user@example.com")
    }

    @Test
    fun `verifying a token marks the account verified`() {
        service.sendVerificationEmail(user.id, user.email)
        val token = tokens.tokens.keys.single()

        service.verify(token)

        assertThat(users.isEmailVerified(user.id)).isTrue()
    }

    @Test
    fun `rejects a token issued for password reset`() {
        val token = UUID.randomUUID()
        tokens.seed(
            OneTimeToken(token, user.id, TokenPurpose.PASSWORD_RESET, Instant.now(clock).plusSeconds(3600), null)
        )

        assertThatThrownBy { service.verify(token) }.isInstanceOf(InvalidTokenException::class.java)
    }
}
