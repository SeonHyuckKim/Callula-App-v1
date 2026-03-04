package org.example.callulaserver.domains.withdrwal.service

import org.example.callulaserver.common.exception.CustomException
import org.example.callulaserver.common.exception.ErrorCode
import org.example.callulaserver.common.logging.Logging
import org.example.callulaserver.common.transaction.Transactional
import org.example.callulaserver.domains.users.repository.UserRepository
import org.example.callulaserver.domains.withdrwal.dto.WithdrawalRequest
import org.example.callulaserver.domains.withdrwal.dto.WithdrawalResponse
import org.example.callulaserver.domains.withdrwal.repository.WithdrawalRepository
import org.example.callulaserver.types.entity.UserWithdrawal
import org.example.callulaserver.types.enums.WithdrawalReason
import org.example.callulaserver.types.enums.WithdrawalStatus
import org.slf4j.Logger
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class WithdrawalService(
    private val withdrawalRepository: WithdrawalRepository,
    private val userRepository: UserRepository,
    private val transaction: Transactional,
    private val logger: Logger = Logging.getLogger(WithdrawalService::class.java),
) {
    fun requestWithdrawal(userUuid: UUID, request: WithdrawalRequest): WithdrawalResponse =
        Logging.logFor(logger) { log ->
            transaction.run {
                log["action"] = "requestWithdrawal"
                log["user_uuid"] = userUuid
                log["reason"] = request.reason

                // 사용자 조회
                val user = userRepository.findByUuidAndIsDeletedFalse(userUuid)
                    ?: throw CustomException(
                        codeInterface = ErrorCode.USER_NOT_FOUND,
                        additionalMessage = "uuid: $userUuid"
                    )

                // 이미 PENDING 상태인 탈퇴 요청이 있는지 확인
                withdrawalRepository.findByUserUuidAndStatus(userUuid, WithdrawalStatus.PENDING)
                    ?.let {
                        log["status"] = "failed"
                        log["error"] = "WITHDRAWAL_ALREADY_REQUESTED"
                        throw CustomException(
                            codeInterface = ErrorCode.WITHDRAWAL_ALREADY_REQUESTED
                        )
                    }

                // OTHER 선택 시 상세 사유 필수
                if (request.reason == WithdrawalReason.OTHER && request.reasonDetail.isNullOrBlank()) {
                    log["status"] = "failed"
                    log["error"] = "WITHDRAWAL_REASON_DETAIL_REQUIRED"
                    throw CustomException(
                        codeInterface = ErrorCode.WITHDRAWAL_REASON_DETAIL_REQUIRED
                    )
                }

                val userWithdrawal = UserWithdrawal(
                    userUuid = user.uuid,
                    email = user.email,
                    phone = user.phone,
                    reason = request.reason,
                    reasonDetail = request.reasonDetail,
                    accountCreatedAt = user.createdAt
                )

                // user soft delete
                user.delete()

                val saved = withdrawalRepository.save(userWithdrawal)

                log["withdrawal_uuid"] = saved.uuid
                log["email"] = user.email
                log["status"] = "success"

                WithdrawalResponse.from(saved)
            }
        }

    // 탈퇴 요청
//    fun requestWithdrawal(userUuid: UUID, request: WithdrawalRequest): WithdrawalResponse {
//        val user = userRepository.findByUuidAndIsDeletedFalse(userUuid)
//            ?: throw CustomException(ErrorCode.USER_NOT_FOUND, "uuid: $userUuid")
//
//        // 이미 PENDING 상태인 탈퇴 요청이 있는지 확인
//        withdrawalRepository.findByUserUuidAndStatus(userUuid, WithdrawalStatus.PENDING)
//            ?.let { throw CustomException(ErrorCode.WITHDRAWAL_ALREADY_REQUESTED) }
//
//        // OTHER 선택 시 상세 사유 필수
//        if (request.reason == WithdrawalReason.OTHER && request.reasonDetail.isNullOrBlank()) {
//            throw CustomException(ErrorCode.WITHDRAWAL_REASON_DETAIL_REQUIRED)
//        }
//
//        val userWithdrawal = UserWithdrawal(
//            userUuid = user.uuid,
//            email = user.email,
//            phone = user.phone,
//            reason = request.reason,
//            reasonDetail = request.reasonDetail,
//            accountCreatedAt = user.createdAt
//        )
//        // user soft delete
//        user.delete()
//
//        val saved = withdrawalRepository.save(userWithdrawal)
//        logger.info("Withdrawal requested: ${saved.uuid}, user: $userUuid")
//
//        return WithdrawalResponse.from(saved)
//    }
    fun cancelWithdrawal(userUuid: UUID): WithdrawalResponse =
        Logging.logFor(logger) { log ->
            transaction.run {
                log["action"] = "cancelWithdrawal"
                log["user_uuid"] = userUuid

                val userWithdrawal = withdrawalRepository.findByUserUuidAndStatus(userUuid, WithdrawalStatus.PENDING)
                    ?: throw CustomException(
                        codeInterface = ErrorCode.WITHDRAWAL_NOT_FOUND,
                        additionalMessage = "uuid: $userUuid"
                    )

                log["withdrawal_uuid"] = userWithdrawal.uuid
                log["before_status"] = userWithdrawal.status

                userWithdrawal.cancel()

                // User 복구
                val user = userRepository.findByUuid(userUuid)
                    ?: throw CustomException(
                        codeInterface = ErrorCode.USER_NOT_FOUND,
                        additionalMessage = "uuid: $userUuid"
                    )

                log["before_is_deleted"] = user.isDeleted
                user.restore()
                log["after_is_deleted"] = user.isDeleted

                log["after_status"] = userWithdrawal.status
                log["status"] = "success"

                WithdrawalResponse.from(userWithdrawal)
            }
        }
    // 탈퇴 취소
//    fun cancelWithdrawal(userUuid: UUID): WithdrawalResponse {
//        val userWithdrawal = withdrawalRepository.findByUserUuidAndStatus(userUuid, WithdrawalStatus.PENDING)
//            ?: throw CustomException(ErrorCode.WITHDRAWAL_NOT_FOUND)
//
//        userWithdrawal.cancel()
//
//        // User 복구
//        val user = userRepository.findById(userUuid).orElseThrow {
//            CustomException(
//                ErrorCode.USER_NOT_FOUND,
//                "uuid: $userUuid"
//            )
//        }
//
//        user.restore()
//
//        logger.info("Withdrawal cancelled: ${userWithdrawal.uuid}, user: $userUuid")
//
//        return WithdrawalResponse.from(userWithdrawal)
//    }

    // 탈퇴 상태 조회
    fun getWithdrawalStatus(userUuid: UUID): WithdrawalResponse {
        val withdrawal = withdrawalRepository.findByUserUuidAndStatus(userUuid, WithdrawalStatus.PENDING)
            ?: throw CustomException(ErrorCode.WITHDRAWAL_NOT_FOUND)

        return WithdrawalResponse.from(withdrawal)
    }

    // 물리적 삭제(스케줄러에서 호출)
    fun processExpiredWithdrawals() =
        Logging.logFor(logger) { log ->
            transaction.run {
                log["action"] = "processExpiredWithdrawals"

                val now = java.time.LocalDateTime.now()
                //retention period(30일) PENDING 건 조회
                val expiredWithdrawals = withdrawalRepository.findAllByStatusAndRequestedAtBefore(WithdrawalStatus.PENDING,now.minusDays(30))

                log["expired_count"] = expiredWithdrawals.size

                expiredWithdrawals.forEach { withdrawal ->
                    // User 물리적 삭제
                    userRepository.deleteById(withdrawal.userUuid)
                    // 상태를 COMPLETED로 변경 (UserWithdrawal 설정)
                    withdrawal.complete()
                }
                log["status"] = "success"
            }
        }
}
