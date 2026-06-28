package org.maxizenit.maxigram.wellbeing.web

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.maxizenit.maxigram.wellbeing.WellbeingService
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.util.UUID

/** Full block: while a self-restraint is active, the user cannot reach the app (423 Locked). */
@Component
class RestraintEnforcementInterceptor(private val service: WellbeingService) : HandlerInterceptor {

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication is JwtAuthenticationToken) {
            val userId = UUID.fromString(authentication.token.subject)
            if (service.isRestrained(userId)) {
                response.status = HttpStatus.LOCKED.value()
                response.contentType = MediaType.APPLICATION_JSON_VALUE
                response.writer.write("""{"error":"Self-restraint is active"}""")
                return false
            }
        }
        return true
    }
}

@Configuration
class WellbeingWebConfig(private val interceptor: RestraintEnforcementInterceptor) : WebMvcConfigurer {

    override fun addInterceptors(registry: InterceptorRegistry) {
        // Block the whole app except the wellbeing endpoints, so the user can still see the restraint.
        registry.addInterceptor(interceptor)
            .addPathPatterns("/api/**")
            .excludePathPatterns("/api/wellbeing/**")
    }
}
