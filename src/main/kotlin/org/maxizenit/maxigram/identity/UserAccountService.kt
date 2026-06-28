package org.maxizenit.maxigram.identity

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.Instant
import java.util.UUID

@Service
class UserAccountService(
    private val repository: UserAccountRepository,
    private val passwordEncoder: PasswordEncoder,
    private val clock: Clock = Clock.systemUTC(),
) {

    /**
     * Registers a new account. Email is normalized (trimmed, lower-cased). The raw password is
     * checked against the strength policy and stored only as a hash.
     */
    fun register(email: String, rawPassword: String): AppUser {
        val normalizedEmail = email.trim().lowercase()
        if (rawPassword.length < MIN_PASSWORD_LENGTH) {
            throw WeakPasswordException("Password must be at least $MIN_PASSWORD_LENGTH characters")
        }
        if (repository.existsByEmail(normalizedEmail)) {
            throw EmailAlreadyUsedException(normalizedEmail)
        }
        val passwordHash = requireNotNull(passwordEncoder.encode(rawPassword)) {
            "Password encoder returned null"
        }
        return repository.insert(UUID.randomUUID(), normalizedEmail, passwordHash, Instant.now(clock))
    }

    companion object {
        const val MIN_PASSWORD_LENGTH = 8
    }
}
