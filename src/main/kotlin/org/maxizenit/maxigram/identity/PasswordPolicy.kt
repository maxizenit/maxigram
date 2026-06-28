package org.maxizenit.maxigram.identity

/** Central password strength policy, shared by registration and password reset. */
object PasswordPolicy {

    const val MIN_LENGTH = 8

    fun check(rawPassword: String) {
        if (rawPassword.length < MIN_LENGTH) {
            throw WeakPasswordException("Password must be at least $MIN_LENGTH characters")
        }
    }
}
