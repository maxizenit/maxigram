package org.maxizenit.maxigram.identity

import org.maxizenit.maxigram.common.EmailSender
import java.time.Instant
import java.util.UUID

class InMemoryUserAccountRepository : UserAccountRepository {

    private class Row(
        val id: UUID,
        val email: String,
        var passwordHash: String,
        var emailVerified: Boolean,
        val createdAt: Instant,
    )

    private val rows = linkedMapOf<UUID, Row>()
    var lastInsertedEmail: String? = null
    var lastInsertedHash: String? = null

    override fun existsByEmail(email: String): Boolean = rows.values.any { it.email == email }

    override fun insert(id: UUID, email: String, passwordHash: String, createdAt: Instant): AppUser {
        rows[id] = Row(id, email, passwordHash, false, createdAt)
        lastInsertedEmail = email
        lastInsertedHash = passwordHash
        return AppUser(id, email, emailVerified = false, createdAt = createdAt)
    }

    override fun findByEmail(email: String): AppUser? =
        rows.values.firstOrNull { it.email == email }
            ?.let { AppUser(it.id, it.email, it.emailVerified, it.createdAt) }

    override fun findCredentialsByEmail(email: String): UserCredentials? =
        rows.values.firstOrNull { it.email == email }
            ?.let { UserCredentials(it.id, it.email, it.passwordHash, it.emailVerified) }

    override fun markEmailVerified(userId: UUID) {
        rows[userId]?.emailVerified = true
    }

    override fun updatePasswordHash(userId: UUID, passwordHash: String) {
        rows[userId]?.passwordHash = passwordHash
    }

    fun passwordHashOf(userId: UUID): String? = rows[userId]?.passwordHash

    fun isEmailVerified(userId: UUID): Boolean? = rows[userId]?.emailVerified
}

class InMemoryOneTimeTokenRepository : OneTimeTokenRepository {

    val tokens = linkedMapOf<UUID, OneTimeToken>()

    override fun insert(token: UUID, userId: UUID, purpose: TokenPurpose, expiresAt: Instant) {
        tokens[token] = OneTimeToken(token, userId, purpose, expiresAt, consumedAt = null)
    }

    override fun find(token: UUID): OneTimeToken? = tokens[token]

    override fun markConsumed(token: UUID, consumedAt: Instant) {
        tokens[token]?.let { tokens[token] = it.copy(consumedAt = consumedAt) }
    }

    fun seed(token: OneTimeToken) {
        tokens[token.token] = token
    }
}

class RecordingEmailSender : EmailSender {

    data class Sent(val to: String, val subject: String, val body: String)

    val sent = mutableListOf<Sent>()

    override fun send(to: String, subject: String, body: String) {
        sent += Sent(to, subject, body)
    }

    fun last(): Sent = sent.last()
}
