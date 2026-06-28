package org.maxizenit.maxigram.identity

import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class AppUserDetailsService(private val repository: UserAccountRepository) : UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails {
        val credentials =
            repository.findCredentialsByEmail(username.trim().lowercase())
                ?: throw UsernameNotFoundException("No account for $username")
        return AppUserDetails(
            id = credentials.id,
            emailAddress = credentials.email,
            passwordHash = credentials.passwordHash,
            emailVerified = credentials.emailVerified,
        )
    }
}
