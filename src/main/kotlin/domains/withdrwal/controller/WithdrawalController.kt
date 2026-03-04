package org.example.callulaserver.domains.withdrwal.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.example.callulaserver.domains.withdrwal.dto.WithdrawalRequest
import org.example.callulaserver.domains.withdrwal.dto.WithdrawalResponse
import org.example.callulaserver.domains.withdrwal.service.WithdrawalService
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

@Tag(name = "회원탈퇴")
@RestController
@RequestMapping("/api/withdrawal/v1")
class WithdrawalController(
    private val withdrawalService: WithdrawalService,
) {
    @Operation(
        summary = "탈퇴요청",
        description = "유저가 탈퇴를 요청하는 API 입니다."
    )
    @PostMapping("/{userUuid}")
    fun requestWithdrawal(
        @PathVariable userUuid: UUID,
        @RequestBody request: WithdrawalRequest,
    ): Response<WithdrawalResponse> {
        val result = withdrawalService.requestWithdrawal(userUuid, request)

        return ResponseProvider.success(result)
    }

    @Operation(
        summary = "탈퇴취소",
        description = "PENDING 상태의 탈퇴요청을 취소합니다."
    )
    @PatchMapping("/{userUuid}/cancel")
    fun cancelWithdrawal(
        @PathVariable userUuid: UUID,
    ): Response<WithdrawalResponse> {
        val result = withdrawalService.cancelWithdrawal(userUuid)

        return ResponseProvider.success(result)
    }

    @Operation(
        summary = "탈퇴상태 조회",
        description = "현재 탈퇴 진행 상태를 조회합니다."
    )
    @GetMapping("/{userUuid}")
    fun getWithdrawalStatus(
        @PathVariable userUuid: UUID,
    ): Response<WithdrawalResponse> {
        val result = withdrawalService.getWithdrawalStatus(userUuid)

        return ResponseProvider.success(result)
    }
}
