package org.maxizenit.maxigram.identity

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Component
import java.util.UUID

/** Resolves the authenticated account id from the JWT (`sub`) of the current request. */
@Component
class CurrentUser {

    fun id(): UUID {
        val authentication =
            SecurityContextHolder.getContext().authentication
                ?: throw IllegalStateException("No authenticated user")
        val subject =
            when (authentication) {
                is JwtAuthenticationToken -> authentication.token.subject
                else -> authentication.name
            }
        return UUID.fromString(subject)
    }
}
