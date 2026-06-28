package org.maxizenit.maxigram.identity

import org.jooq.DSLContext
import org.maxizenit.maxigram.jooq.Tables.APP_USER
import org.springframework.stereotype.Repository
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID

@Repository
class JooqUserAccountRepository(private val dsl: DSLContext) : UserAccountRepository {

    override fun existsByEmail(email: String): Boolean =
        dsl.fetchExists(APP_USER, APP_USER.EMAIL.eq(email))

    override fun insert(id: UUID, email: String, passwordHash: String, createdAt: Instant): AppUser {
        dsl.insertInto(APP_USER)
            .set(APP_USER.ID, id)
            .set(APP_USER.EMAIL, email)
            .set(APP_USER.PASSWORD_HASH, passwordHash)
            .set(APP_USER.EMAIL_VERIFIED, false)
            .set(APP_USER.CREATED_AT, createdAt.atOffset(ZoneOffset.UTC))
            .execute()
        return AppUser(id, email, emailVerified = false, createdAt = createdAt)
    }

    override fun findByEmail(email: String): AppUser? {
        val record =
            dsl.select(APP_USER.ID, APP_USER.EMAIL, APP_USER.EMAIL_VERIFIED, APP_USER.CREATED_AT)
                .from(APP_USER)
                .where(APP_USER.EMAIL.eq(email))
                .fetchOne() ?: return null
        return AppUser(
            id = record.get(APP_USER.ID),
            email = record.get(APP_USER.EMAIL),
            emailVerified = record.get(APP_USER.EMAIL_VERIFIED),
            createdAt = record.get(APP_USER.CREATED_AT).toInstant(),
        )
    }

    override fun findCredentialsByEmail(email: String): UserCredentials? {
        val record =
            dsl.select(APP_USER.ID, APP_USER.EMAIL, APP_USER.PASSWORD_HASH, APP_USER.EMAIL_VERIFIED)
                .from(APP_USER)
                .where(APP_USER.EMAIL.eq(email))
                .fetchOne() ?: return null
        return UserCredentials(
            id = record.get(APP_USER.ID),
            email = record.get(APP_USER.EMAIL),
            passwordHash = record.get(APP_USER.PASSWORD_HASH),
            emailVerified = record.get(APP_USER.EMAIL_VERIFIED),
        )
    }
}
