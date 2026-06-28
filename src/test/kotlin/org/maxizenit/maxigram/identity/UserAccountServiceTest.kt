package org.maxizenit.maxigram.identity

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder

class UserAccountServiceTest {

    private val repository = InMemoryUserAccountRepository()
    private val encoder = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8()
    private val service = UserAccountService(repository, encoder)

    @Test
    fun `registers a new user with normalized email and hashed password`() {
        val user = service.register("  New@Example.com  ", "supersecret")

        assertThat(user.email).isEqualTo("new@example.com")
        assertThat(user.emailVerified).isFalse()
        assertThat(repository.lastInsertedEmail).isEqualTo("new@example.com")

        val storedHash = repository.lastInsertedHash!!
        assertThat(storedHash).isNotEqualTo("supersecret")
        assertThat(encoder.matches("supersecret", storedHash)).isTrue()
    }

    @Test
    fun `rejects a duplicate email regardless of case`() {
        service.register("dup@example.com", "supersecret")

        assertThatThrownBy { service.register("DUP@example.com", "anotherpass") }
            .isInstanceOf(EmailAlreadyUsedException::class.java)
    }

    @Test
    fun `rejects a password shorter than the policy minimum`() {
        assertThatThrownBy { service.register("weak@example.com", "short") }
            .isInstanceOf(WeakPasswordException::class.java)

        assertThat(repository.lastInsertedEmail).isNull()
    }
}
