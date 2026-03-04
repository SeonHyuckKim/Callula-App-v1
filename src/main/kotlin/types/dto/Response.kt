package org.example.callulaserver.types.dto

import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.http.HttpStatus

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Response<T>(
    val code: Int,
    val message: String,
    val result: T? = null,
    val timestamp: Long = System.currentTimeMillis(),
)

object ResponseProvider {

    fun <T> success(result: T): Response<T> {
        return Response(
            code = HttpStatus.OK.value(),
            message = "SUCCESS",
            result = result
        )
    }

    fun success(): Response<Unit> {
        return Response(
            code = HttpStatus.OK.value(),
            message = "SUCCESS",
            result = Unit
        )
    }

    fun <T> fail(code: HttpStatus, message: String, result: T? = null): Response<T> {
        return Response(
            code = code.value(),
            message = message,
            result = result
        )
    }

    fun <T> fail(message: String): Response<T> {
        return Response(
            code = HttpStatus.BAD_REQUEST.value(),
            message = message,
            result = null
        )
    }

    fun <T> fromException(exception: org.example.callulaserver.common.exception.CustomException): Response<T> {
        return Response(
            code = exception.getCode(),
            message = exception.codeInterface.message,
            result = exception.additionalMessage as? T
        )
    }
}
