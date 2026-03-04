package org.example.callulaserver

import io.github.cdimascio.dotenv.dotenv
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling // 물리적 삭제를 위한 날짜 스케줄링
@SpringBootApplication
class CallulaServerApplication

fun main(args: Array<String>) {
    println("callula 서버가 시작되었습니다.")
    val logger = LoggerFactory.getLogger("Main")

    try {
        val dotenv = dotenv {
            ignoreIfMissing = true
        }

        val entries = dotenv.entries()
        if (entries.isNotEmpty()) {
            logger.info(".env 파일 로드 완료: ${entries.size}개의 환경변수 로드됨")
            entries.forEach { entry ->
                System.setProperty(entry.key, entry.value)
                logger.info("Loaded env: ${entry.key}")
            }
        } else {
            logger.warn(".env 파일을 찾지 못했습니다.")
        }
    } catch (e: Exception) {
        logger.error(".env 파일 로드 실패: ${e.message}")
    }

    runApplication<CallulaServerApplication>(*args)
}
