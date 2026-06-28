package org.maxizenit.maxigram.identity

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder
import java.time.Instant
import java.util.UUID

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

    private class InMemoryUserAccountRepository : UserAccountRepository {
        private val emails = mutableSetOf<String>()
        var lastInsertedEmail: String? = null
        var lastInsertedHash: String? = null

        override fun existsByEmail(email: String) = emails.contains(email)

        override fun insert(id: UUID, email: String, passwordHash: String, createdAt: Instant): AppUser {
            emails.add(email)
            lastInsertedEmail = email
            lastInsertedHash = passwordHash
            return AppUser(id, email, emailVerified = false, createdAt = createdAt)
        }

        override fun findByEmail(email: String): AppUser? = null

        override fun findCredentialsByEmail(email: String): UserCredentials? = null
    }
}
