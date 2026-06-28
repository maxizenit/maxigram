package org.maxizenit.maxigram.identity

/** Raised when registering with an email that already has an account. */
class EmailAlreadyUsedException(email: String) :
    RuntimeException("Email already in use: $email")

/** Raised when a chosen password does not meet the strength policy. */
class WeakPasswordException(message: String) : RuntimeException(message)
