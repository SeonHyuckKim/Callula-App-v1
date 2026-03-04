package org.example.callulaserver.common.httpClient

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.example.callulaserver.common.exception.CustomException
import org.example.callulaserver.common.exception.ErrorCode
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.io.IOException

@Component
class CallClient(
    private val httpClient: OkHttpClient,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun get(uri: String, headers: Map<String, String> = emptyMap()): String {
        val request = buildRequest(uri, headers) { url(uri) }
        return executeRequest(request)
    }

    fun post(
        uri: String,
        headers: Map<String, String> = emptyMap(),
        body: RequestBody,
    ): String {
        val request = buildRequest(uri, headers) {
            url(uri)
            post(body)
        }
        return executeRequest(request)
    }

    fun postJson(
        uri: String,
        headers: Map<String, String> = emptyMap(),
        jsonBody: String,
    ): String {
        val body = jsonBody.toRequestBody("application/json".toMediaType())
        return post(uri, headers, body)
    }

    fun put(
        uri: String,
        headers: Map<String, String> = emptyMap(),
        body: RequestBody,
    ): String {
        val request = buildRequest(uri, headers) {
            url(uri)
            put(body)
        }
        return executeRequest(request)
    }

    fun delete(uri: String, headers: Map<String, String> = emptyMap()): String {
        val request = buildRequest(uri, headers) {
            url(uri)
            delete()
        }
        return executeRequest(request)
    }

    private fun buildRequest(
        uri: String,
        headers: Map<String, String>,
        block: Request.Builder.() -> Unit,
    ): Request {
        return Request.Builder().apply {
            block()
            headers.forEach { (key, value) -> addHeader(key, value) }
        }.build()
    }

    private fun executeRequest(request: Request): String {
        val startTime = System.currentTimeMillis()

        return try {
            httpClient.newCall(request).execute().use { response ->
                val duration = System.currentTimeMillis() - startTime
                logger.debug(
                    "HTTP ${request.method} ${request.url} - ${response.code} (${duration}ms)"
                )

                handleResponse(response, request.url.toString())
            }
        } catch (e: IOException) {
            logger.error("HTTP request failed: ${request.url}", e)
            throw CustomException(
                codeInterface = ErrorCode.FAILED_TO_CALL_CLIENT,
                additionalMessage = "URL: ${request.url}, Error: ${e.message}"
            )
        } catch (e: CustomException) {
            // CustomException은 그대로 전파
            throw e
        } catch (e: Exception) {
            logger.error("Unexpected error during HTTP request: ${request.url}", e)
            throw CustomException(
                codeInterface = ErrorCode.FAILED_TO_CALL_CLIENT,
                additionalMessage = "Unexpected error: ${e.message}"
            )
        }
    }

    private fun handleResponse(response: Response, url: String): String {
        if (!response.isSuccessful) {
            val errorBody = response.body?.string() ?: "unknown error"
            val errorMsg = "HTTP ${response.code} from $url: $errorBody"

            logger.warn("HTTP error: $errorMsg")

            throw CustomException(
                codeInterface = ErrorCode.FAILED_TO_CALL_CLIENT,
                additionalMessage = errorMsg
            )
        }

        return response.body?.string()
            ?: throw CustomException(
                codeInterface = ErrorCode.CALL_RESULT_BODY_NULL,
                additionalMessage = "URL: $url"
            )
    }
}
