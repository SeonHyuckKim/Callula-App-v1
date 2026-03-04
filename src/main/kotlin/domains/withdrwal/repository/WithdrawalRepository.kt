package org.example.callulaserver.domains.withdrwal.repository

import org.example.callulaserver.types.entity.UserWithdrawal
import org.example.callulaserver.types.enums.WithdrawalStatus
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime
import java.util.UUID

interface WithdrawalRepository : JpaRepository<UserWithdrawal, UUID> {

// 유저의 PENDING 상태 탈퇴 요청 조회 (중복 요청 방지용)
    fun findByUserUuidAndStatus(userUUID: UUID, status: WithdrawalStatus): UserWithdrawal?

// retentionPeriod 지난 PENDING 건 조회 (배치 삭제용)
    fun findAllByStatusAndRequestedAtBefore(status: WithdrawalStatus, deTime: LocalDateTime): List<UserWithdrawal>
}
