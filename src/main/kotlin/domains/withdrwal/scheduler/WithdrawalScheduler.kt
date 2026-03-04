package org.example.callulaserver.domains.withdrwal.scheduler

import org.example.callulaserver.domains.withdrwal.service.WithdrawalService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class WithdrawalScheduler(
    private val withdrawalService: WithdrawalService,
){
    @Scheduled(cron = "0 0 0 * * *")    // 매일 자정에 retention Period 지난 아이디 삭제
    fun deleteExpiredUsers() {
        withdrawalService.processExpiredWithdrawals()
    }
}

