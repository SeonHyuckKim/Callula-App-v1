package org.example.callulaserver.domains.auth.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema
data class LoginRequest(
    @Schema(
        description = "로그인 할 이메일",
        example = "test@naver.com",
        required = true
    )
    val email: String,
    @Schema(
        description = "비밀번호",
        example = "qweasd455!@",
        required = true
    )
    val password: String,
)
data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
)
