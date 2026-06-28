package org.maxizenit.maxigram.identity

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID

class OneTimeTokenServiceTest {

    private val now = Instant.parse("2026-06-28T12:00:00Z")
    private val repository = InMemoryOneTimeTokenRepository()
    private val service = OneTimeTokenService(repository, Clock.fixed(now, ZoneOffset.UTC))

    @Test
    fun `issues a token with the requested ttl`() {
        val userId = UUID.randomUUID()
        val token = service.issue(userId, TokenPurpose.EMAIL_VERIFICATION, Duration.ofHours(2))

        val stored = repository.find(token)!!
        assertThat(stored.userId).isEqualTo(userId)
        assertThat(stored.purpose).isEqualTo(TokenPurpose.EMAIL_VERIFICATION)
        assertThat(stored.expiresAt).isEqualTo(now.plus(Duration.ofHours(2)))
    }

    @Test
    fun `consumes a valid token once`() {
        val token = service.issue(UUID.randomUUID(), TokenPurpose.PASSWORD_RESET, Duration.ofHours(1))

        service.consume(token, TokenPurpose.PASSWORD_RESET)

        assertThat(repository.find(token)!!.consumedAt).isEqualTo(now)
        assertThatThrownBy { service.consume(token, TokenPurpose.PASSWORD_RESET) }
            .isInstanceOf(InvalidTokenException::class.java)
    }

    @Test
    fun `rejects an unknown token`() {
        assertThatThrownBy { service.consume(UUID.randomUUID(), TokenPurpose.EMAIL_VERIFICATION) }
            .isInstanceOf(InvalidTokenException::class.java)
    }

    @Test
    fun `rejects a token used for the wrong purpose`() {
        val token = service.issue(UUID.randomUUID(), TokenPurpose.EMAIL_VERIFICATION, Duration.ofHours(1))

        assertThatThrownBy { service.consume(token, TokenPurpose.PASSWORD_RESET) }
            .isInstanceOf(InvalidTokenException::class.java)
    }

    @Test
    fun `rejects an expired token`() {
        val token = UUID.randomUUID()
        repository.seed(
            OneTimeToken(token, UUID.randomUUID(), TokenPurpose.EMAIL_VERIFICATION, now.minusSeconds(1), null)
        )

        assertThatThrownBy { service.consume(token, TokenPurpose.EMAIL_VERIFICATION) }
            .isInstanceOf(InvalidTokenException::class.java)
    }
}
