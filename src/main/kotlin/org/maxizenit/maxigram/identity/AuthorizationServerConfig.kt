package org.maxizenit.maxigram.identity

import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.source.ImmutableJWKSet
import com.nimbusds.jose.jwk.source.JWKSource
import com.nimbusds.jose.proc.SecurityContext
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.http.MediaType
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration
import org.springframework.security.config.annotation.web.configurers.oauth2.server.authorization.OAuth2AuthorizationServerConfigurer
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.security.oauth2.core.ClientAuthenticationMethod
import org.springframework.security.oauth2.core.oidc.OidcScopes
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher
import java.nio.file.Path
import java.time.Duration
import java.util.UUID

@Configuration
class AuthorizationServerConfig {

    @Bean
    @Order(1)
    fun authorizationServerSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        val authorizationServerConfigurer = OAuth2AuthorizationServerConfigurer()
        val endpointsMatcher = authorizationServerConfigurer.endpointsMatcher
        http.securityMatcher(endpointsMatcher)
        http.with(authorizationServerConfigurer) { configurer ->
            configurer.oidc(Customizer.withDefaults())
        }
        http.cors(Customizer.withDefaults())
        http.authorizeHttpRequests { it.anyRequest().authenticated() }
        http.csrf { it.ignoringRequestMatchers(endpointsMatcher) }
        http.exceptionHandling { exceptions ->
            exceptions.defaultAuthenticationEntryPointFor(
                LoginUrlAuthenticationEntryPoint("/login"),
                MediaTypeRequestMatcher(MediaType.TEXT_HTML),
            )
        }
        http.oauth2ResourceServer { it.jwt(Customizer.withDefaults()) }
        return http.build()
    }

    @Bean
    fun registeredClientRepository(
        @Value("\${maxigram.spa.base-url:http://localhost:5173}") spaBaseUrl: String,
    ): RegisteredClientRepository {
        val spaClient =
            RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("maxigram-spa")
                .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("$spaBaseUrl/callback")
                .redirectUri("$spaBaseUrl/silent-renew.html")
                .postLogoutRedirectUri("$spaBaseUrl/")
                .scope(OidcScopes.OPENID)
                .scope(OidcScopes.PROFILE)
                .scope(OidcScopes.EMAIL)
                .clientSettings(
                    ClientSettings.builder()
                        .requireProofKey(true)
                        .requireAuthorizationConsent(false)
                        .build()
                )
                // Public clients get no refresh token (by SAS design, gh-297): the SPA renews
                // silently via prompt=none against the live AS session instead.
                .tokenSettings(
                    TokenSettings.builder()
                        .accessTokenTimeToLive(Duration.ofMinutes(10))
                        .build()
                )
                .build()
        return InMemoryRegisteredClientRepository(spaClient)
    }

    @Bean
    fun tokenCustomizer(): OAuth2TokenCustomizer<JwtEncodingContext> =
        OAuth2TokenCustomizer { context ->
            val principal = context.getPrincipal<Authentication>()?.principal
            if (principal is AppUserDetails) {
                context.claims.subject(principal.id.toString())
                context.claims.claim("email", principal.username)
            }
        }

    /** Signing key is persisted when maxigram.jwk.path is set, so restarts keep tokens valid. */
    @Bean
    fun jwkSource(@Value("\${maxigram.jwk.path:}") jwkPath: String): JWKSource<SecurityContext> {
        val rsaKey = loadOrCreateRsaKey(jwkPath.ifBlank { null }?.let { Path.of(it) })
        return ImmutableJWKSet(JWKSet(rsaKey))
    }

    @Bean
    fun jwtDecoder(jwkSource: JWKSource<SecurityContext>): JwtDecoder =
        OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource)

    @Bean
    fun authorizationServerSettings(): AuthorizationServerSettings =
        AuthorizationServerSettings.builder().build()
}
