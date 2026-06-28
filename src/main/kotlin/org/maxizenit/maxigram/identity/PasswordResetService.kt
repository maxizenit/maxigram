package org.maxizenit.maxigram.identity

import org.maxizenit.maxigram.common.EmailSender
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.time.Duration
import java.util.UUID

@Service
class PasswordResetService(
    private val users: UserAccountRepository,
    private val oneTimeTokens: OneTimeTokenService,
    private val emailSender: EmailSender,
    private val passwordEncoder: PasswordEncoder,
) {

    /** Always succeeds silently, even for unknown emails, to avoid leaking which addresses exist. */
    fun requestReset(email: String) {
        val normalizedEmail = email.trim().lowercase()
        val user = users.findByEmail(normalizedEmail) ?: return
        val token = oneTimeTokens.issue(user.id, TokenPurpose.PASSWORD_RESET, TTL)
        emailSender.send(normalizedEmail, "Сброс пароля", "Код сброса пароля: $token")
    }

    fun reset(token: UUID, newPassword: String) {
        // Validate the new password before consuming the token, so a weak password is retryable.
        PasswordPolicy.check(newPassword)
        val record = oneTimeTokens.consume(token, TokenPurpose.PASSWORD_RESET)
        val passwordHash = requireNotNull(passwordEncoder.encode(newPassword)) {
            "Password encoder returned null"
        }
        users.updatePasswordHash(record.userId, passwordHash)
    }

    private companion object {
        val TTL: Duration = Duration.ofHours(1)
    }
}
