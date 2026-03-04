package org.example.callulaserver.common.exception

import org.example.callulaserver.types.dto.Response
import org.example.callulaserver.types.dto.ResponseProvider
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.AuthenticationException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    private val logger = LoggerFactory.getLogger(javaClass)


    @ExceptionHandler(AuthenticationException::class)
    fun handleAuthenticationException(e: AuthenticationException): ResponseEntity<Response<String>> {
        logger.warn("Authentication failed: ${e.message}")

        val response = ResponseProvider.fail<String>(
            code = HttpStatus.UNAUTHORIZED,
            message = "인증이 필요합니다",
            result = e.message
        )

        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(response)
    }


    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDeniedException(e: AccessDeniedException): ResponseEntity<Response<String>> {
        logger.warn("Access denied: ${e.message}")

        val response = ResponseProvider.fail<String>(
            code = HttpStatus.FORBIDDEN,
            message = "접근 권한이 없습니다",
            result = e.message
        )

        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(response)
    }

    // CustomException 처리
    @ExceptionHandler(CustomException::class)
    fun handleCustomException(e: CustomException): ResponseEntity<Response<ErrorDetail>> {
        logger.error("CustomException: ${e.message}", e)

        val errorDetail = ErrorDetail(
            errorCode = e.getCode(),
            errorMessage = e.codeInterface.message,
            details = e.additionalMessage
        )

        val response = ResponseProvider.fail(
            code = HttpStatus.BAD_REQUEST,
            message = e.codeInterface.message,
            result = errorDetail
        )

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(response)
    }

    // Validation 에러 처리
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(e: MethodArgumentNotValidException): ResponseEntity<Response<String>> {
        val errors = e.bindingResult.fieldErrors
            .joinToString(", ") { "${it.field}: ${it.defaultMessage}" }

        logger.warn("Validation error: $errors")

        val response = ResponseProvider.fail<String>(
            code = HttpStatus.BAD_REQUEST,
            message = "입력값 검증 실패",
            result = errors
        )

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(response)
    }


    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ResponseEntity<Response<String>> {
        logger.error("Unexpected error: ${e.message}", e)

        val response = ResponseProvider.fail<String>(
            code = HttpStatus.INTERNAL_SERVER_ERROR,
            message = "서버 내부 오류가 발생했습니다",
            result = e.message
        )

        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(response)
    }
}

// 에러 상세 정보
data class ErrorDetail(
    val errorCode: Int,
    val errorMessage: String,
    val details: String? = null,
)
