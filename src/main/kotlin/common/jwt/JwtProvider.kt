package org.example.callulaserver.common.jwt

import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.example.callulaserver.common.exception.CustomException
import org.example.callulaserver.common.exception.ErrorCode
import org.example.callulaserver.config.JwtProperties
import org.springframework.stereotype.Component
import java.util.Base64
import java.util.Date
import java.util.UUID

@Component
class JwtProvider(
    private val jwtProperties: JwtProperties,
) {
    private val key by lazy {
        Keys.hmacShaKeyFor(Base64.getDecoder().decode(jwtProperties.secret))
    }

    fun generateAccessToken(uuid: UUID): String {
        return generateToken(
            uuid,
            jwtProperties.accessTokenExpiration,
            "access"
        )
    }

    fun generateRefreshToken(uuid: UUID): String {
        return generateToken(
            uuid,
            jwtProperties.refreshTokenExpiration,
            "refresh"
        )
    }

    fun validateToken(token: String): Boolean {
        try {
            getClaims(token)
            return true
        } catch (e: ExpiredJwtException) {
            throw CustomException(
                codeInterface = ErrorCode.TOKEN_IS_EXPIRED,
                additionalMessage = "token is expired"
            )
        } catch (e: Exception) {
            throw CustomException(
                codeInterface = ErrorCode.TOKEN_IS_INVALID,
                additionalMessage = "token is invalid"
            )
        }
    }

    fun getUuidFromToken(token: String): UUID {
        val claims = getClaims(token)
        return UUID.fromString(claims.subject)
    }

    fun getTokenType(token: String): String {
        return getClaims(token).get("type", String::class.java)
    }

    private fun generateToken(uuid: UUID, expiration: Long, type: String):
        String {
        val now = Date()
        val expireDate = Date(now.time + expiration)

        return Jwts.builder()
            .subject(uuid.toString())
            .claim("type", type)
            .issuedAt(now)
            .expiration(expireDate)
            .signWith(key)
            .compact()
    }

    private fun getClaims(token: String): Claims {
        return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload
    }
}
