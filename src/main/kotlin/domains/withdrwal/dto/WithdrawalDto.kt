package org.example.callulaserver.domains.withdrwal.dto

import io.swagger.v3.oas.annotations.media.Schema
import org.example.callulaserver.types.entity.UserWithdrawal
import org.example.callulaserver.types.enums.WithdrawalReason
import org.example.callulaserver.types.enums.WithdrawalStatus
import java.time.LocalDateTime
import java.util.UUID

@Schema(description = "탈퇴 요청")
data class WithdrawalRequest( // userUuid, email,phone, accountCreatedAt 은 서비스에서 User 조회 후 채우면 됨.
    @Schema(
        description = "탈퇴 사유",
        example = "NOT_USEFUL",
        required = true
    )
    val reason: WithdrawalReason,
    @Schema(
        description = "기타 사유 상세 (OTHER 선택 시)",
        example = "서비스를 더 이상 사용하지 않습니다"
    )
    val reasonDetail: String? = null, // OTHER 선택시 상세 사유
)

@Schema(description = "탈퇴 응답")
data class WithdrawalResponse(
    @Schema(
        description = "탈퇴 요청 UUID",
        example = "550e8400-e29b-41d4-a716-446655440000",
        required = true
    )
    val uuid: UUID,
    @Schema(
        description = "탈퇴 사유",
        example = "NOT_USEFUL",
        required = true
    )
    val reason: WithdrawalReason,
    @Schema(
        description = "기타 사유 상세",
        example = "서비스를 더 이상 사용하지 않습니다"
    )
    val reasonDetail: String?,
    @Schema(
        description = "탈퇴 상태",
        example = "PENDING",
        required = true
    )
    val status: WithdrawalStatus,
    @Schema(
        description = "탈퇴 요청일",
        example = "2026-02-16T12:00:00",
        required = true
    )
    val requestedAt: LocalDateTime,
    @Schema(
        description = "예정 삭제일",
        example = "2026-03-18T12:00:00",
        required = true
    )
    val scheduleDeletionDate: LocalDateTime,
) {
    companion object {
        fun from(withdrawal: UserWithdrawal): WithdrawalResponse {
            return WithdrawalResponse(
                uuid = withdrawal.uuid,
                reason = withdrawal.reason,
                reasonDetail = withdrawal.reasonDetail,
                status = withdrawal.status,
                requestedAt = withdrawal.requestedAt,
                scheduleDeletionDate =
                withdrawal.calculateScheduledDeletionDate()

            )
        }
    }
}
