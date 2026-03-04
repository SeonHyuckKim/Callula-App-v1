package org.example.callulaserver.types.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.example.callulaserver.types.enums.WithdrawalReason
import org.example.callulaserver.types.enums.WithdrawalStatus
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "user_withdrawal")
class UserWithdrawal(

    @Id
    @Column(name = "uuid", columnDefinition = "BINARY(16)")
    val uuid: UUID = UUID.randomUUID(),

    @Column(name = "user_uuid", nullable = false, columnDefinition = "BINARY(16)")
    val userUuid: UUID,

    @Column(name = "email", nullable = false, length = 100) // 스냅샷
    val email: String,

    @Column(name = "phone", length = 100) // 스냅샷
    val phone: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "reason", nullable = false, length = 50)
    val reason: WithdrawalReason,

    @Column(name = "reason_detail", columnDefinition = "TEXT")
    val reasonDetail: String? = null,

    @Column(name = "requested_at", nullable = false)
    val requestedAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "withdrawal_date")
    var withdrawalDate: LocalDateTime? = null, // 실제 물리적 삭제가 되는 날짜

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    var status: WithdrawalStatus = WithdrawalStatus.PENDING,

    @Column(name = "retention_period", nullable = false)
    val retentionPeriod: Int = 30,

//    @Column(name = "processed_by", columnDefinition = "BINARY(16)") // 관리자 기능 시 필요
//    var processedBy: UUID? = null,

    @Column(name = "account_created_at") // 계정 생성일 (스냅샷)
    val accountCreatedAt: LocalDateTime? = null,

    @UpdateTimestamp
    @Column(name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now(),
) {
    // 탈퇴 완료 처리
    fun complete() {
        this.status = WithdrawalStatus.COMPLETED
        this.withdrawalDate = LocalDateTime.now()
    }

    // 탈퇴 취소
    fun cancel() {
        this.status = WithdrawalStatus.CANCELLED
    }

    // 탈퇴 예정일 계산
    fun calculateScheduledDeletionDate(): LocalDateTime {
        return requestedAt.plusDays(retentionPeriod.toLong())
    }
}
