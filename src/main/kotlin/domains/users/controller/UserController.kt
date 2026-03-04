package org.example.callulaserver.domains.users.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.example.callulaserver.domains.users.dto.CreateUserRequest
import org.example.callulaserver.domains.users.dto.UpdateUserRequest
import org.example.callulaserver.domains.users.dto.UserResponse
import org.example.callulaserver.domains.users.service.UserService
import org.example.callulaserver.types.dto.Response
import org.example.callulaserver.types.dto.ResponseProvider
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@Tag(name = "유저 관련 API")
@RestController
@RequestMapping("/api/users/v1")
class UserController(
    private val userService: UserService,
) {
    @Operation(summary = "유저 회원가입", description = "유저를 회원가입시키는 API입니다.")
    @PostMapping("/signup")
    fun signUp(@RequestBody request: CreateUserRequest): Response<UserResponse> {
        val user = userService.createUser(request)
        return ResponseProvider.success(user)
    }

    @Operation(summary = "사용자 정보 조회", description = "UUID로 사용자 정보를 조회합니다")
    @GetMapping("/{uuid}")
    fun getUserInfo(@PathVariable uuid: UUID): Response<UserResponse> {
        val user = userService.getUserInfo(uuid)
        return ResponseProvider.success(user)
    }

    @Operation(summary = "사용자 정보 수정", description = "로그인된 사용자의 정보를 수정합니다.")
    @PatchMapping("/{uuid}")
    fun updateUser(
        @PathVariable uuid: UUID,
        @RequestBody request: UpdateUserRequest,
    ): Response<UserResponse> {
        val user = userService.updateUser(uuid, request)
        return ResponseProvider.success(user)
    }
}
