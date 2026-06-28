package org.maxizenit.maxigram.identity

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.util.UUID

/** Spring Security principal. Username is the email (login field); [id] is the stable account id. */
class AppUserDetails(
    val id: UUID,
    private val emailAddress: String,
    private val passwordHash: String,
    val emailVerified: Boolean,
) : UserDetails {

    override fun getAuthorities(): Collection<GrantedAuthority> =
        listOf(SimpleGrantedAuthority("ROLE_USER"))

    override fun getPassword(): String = passwordHash

    override fun getUsername(): String = emailAddress

    override fun isAccountNonExpired(): Boolean = true

    override fun isAccountNonLocked(): Boolean = true

    override fun isCredentialsNonExpired(): Boolean = true

    override fun isEnabled(): Boolean = true
}
