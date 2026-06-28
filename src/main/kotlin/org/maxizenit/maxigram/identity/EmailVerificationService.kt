package org.maxizenit.maxigram.identity

import org.maxizenit.maxigram.common.EmailSender
import org.springframework.stereotype.Service
import java.time.Duration
import java.util.UUID

@Service
class EmailVerificationService(
    private val users: UserAccountRepository,
    private val oneTimeTokens: OneTimeTokenService,
    private val emailSender: EmailSender,
) {

    fun sendVerificationEmail(userId: UUID, email: String) {
        val token = oneTimeTokens.issue(userId, TokenPurpose.EMAIL_VERIFICATION, TTL)
        emailSender.send(email, "Подтверждение адреса", "Код подтверждения email: $token")
    }

    fun verify(token: UUID) {
        val record = oneTimeTokens.consume(token, TokenPurpose.EMAIL_VERIFICATION)
        users.markEmailVerified(record.userId)
    }

    private companion object {
        val TTL: Duration = Duration.ofHours(24)
    }
}
