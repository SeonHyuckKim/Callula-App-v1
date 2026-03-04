package org.example.callulaserver.common.logging

import org.example.callulaserver.common.exception.CustomException
import org.example.callulaserver.common.exception.ErrorCode
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object Logging {
    fun <T : Any> getLogger(clazz: Class<T>): Logger = LoggerFactory.getLogger(clazz)

    fun <T> logFor(log: Logger, function: (MutableMap<String, Any>) -> T?): T {
        val logInfo = mutableMapOf<String, Any>()
        logInfo["start_at"] = now()

        val result = function.invoke(logInfo)

        logInfo["end_at"] = now()

        log.info(logInfo.toString())

        return result ?: throw CustomException(ErrorCode.FAILED_TO_INVOKE_IN_LOGGER)
    }

    private fun now(): Long {
        return System.currentTimeMillis()
    }
}
