package org.maxizenit.maxigram.identity

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.http.HttpMethod
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class WebSecurityConfig {

    /** Stateless JWT-protected API. Registration is public; everything else needs a token. */
    @Bean
    @Order(2)
    fun apiSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http.securityMatcher("/api/**")
        http.authorizeHttpRequests { auth ->
            auth.requestMatchers(HttpMethod.POST, "/api/identity/registrations").permitAll()
            auth.requestMatchers(HttpMethod.POST, "/api/identity/email-verifications/**").permitAll()
            auth.requestMatchers(HttpMethod.POST, "/api/identity/password-resets", "/api/identity/password-resets/**")
                .permitAll()
            auth.anyRequest().authenticated()
        }
        http.csrf { it.disable() }
        http.sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
        http.oauth2ResourceServer { it.jwt(Customizer.withDefaults()) }
        return http.build()
    }

    /** Browser-facing chain: form login backing the Authorization Server's login page. */
    @Bean
    @Order(3)
    fun defaultSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http.authorizeHttpRequests { auth ->
            // /ws is the STOMP handshake; the session is authenticated at STOMP CONNECT via JWT.
            auth.requestMatchers("/actuator/**", "/ws/**").permitAll()
            auth.anyRequest().authenticated()
        }
        http.formLogin(Customizer.withDefaults())
        return http.build()
    }
}
