package org.example.callulaserver.domains.auth.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.example.callulaserver.domains.auth.dto.LoginRequest
import org.example.callulaserver.domains.auth.dto.LoginResponse
import org.example.callulaserver.domains.auth.service.AuthService
import org.example.callulaserver.types.dto.Response
import org.example.callulaserver.types.dto.ResponseProvider
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "인증관련 API")
@RestController
@RequestMapping("/api/auth/v1")
class AuthController(
    private val authService: AuthService,
) {
    @Operation(summary = "로그인API", description = "로그인 인증 하는 API입니다.")
    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): Response<LoginResponse> {
        val user = authService.login(request)
        println("유저 정 보 : $request")
        return ResponseProvider.success(user)
    }
}
