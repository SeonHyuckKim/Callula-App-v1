package org.example.callulaserver.domains.auth.service

import org.example.callulaserver.common.exception.CustomException
import org.example.callulaserver.common.exception.ErrorCode
import org.example.callulaserver.common.jwt.JwtProvider
import org.example.callulaserver.domains.auth.dto.LoginRequest
import org.example.callulaserver.domains.auth.dto.LoginResponse
import org.example.callulaserver.domains.users.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder, // ← 추가
    private val jwtProvider: JwtProvider,
) {
    @Transactional
    fun login(request: LoginRequest): LoginResponse {
        val user = userRepository.findByEmail(request.email)
            ?: throw CustomException(
                codeInterface = ErrorCode.USER_NOT_FOUND,
                additionalMessage = "email: ${request.email}"
            )
        if (!passwordEncoder.matches(request.password, user.password)) {
            throw CustomException(
                codeInterface = ErrorCode.INVALID_PASSWORD,
                additionalMessage = "email: ${request.email}"
            )
        }
        val accessToken =
            jwtProvider.generateAccessToken(user.uuid)
        val refreshToken =
            jwtProvider.generateRefreshToken(user.uuid)

        return LoginResponse(
            accessToken = accessToken,
            refreshToken = refreshToken
        )
    }
}
