package org.example.callulaserver.common.jwt

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtProvider: JwtProvider,
) : OncePerRequestFilter() {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val token = resolveToken(request)

        logger.info("=== JWT Authentication Filter ===")
        logger.info("Request URI: ${request.requestURI}")
        logger.info("Token: $token")

        if (token != null && jwtProvider.validateToken(token) &&
            jwtProvider.getTokenType(token) == "access"
        ) {
            val uuid = jwtProvider.getUuidFromToken(token)
            logger.info("Authenticated UUID: $uuid") // ← 추가

            val authentication = UsernamePasswordAuthenticationToken(
                uuid,
                null,
                emptyList()
            )
            SecurityContextHolder.getContext().authentication = authentication
        } else {
            logger.warn("Authentication failed - no token or invalid") // ← 추가
        }

        filterChain.doFilter(request, response)
    }

    private fun resolveToken(request: HttpServletRequest): String? {
        val bearer = request.getHeader("Authorization")
        logger.debug("Authorization header: $bearer") // ← 추가

        return if (bearer != null && bearer.startsWith("Bearer ")) {
            bearer.substring(7)
        } else {
            null
        }
    }
}
