package org.example.callulaserver.config

import okhttp3.ConnectionPool
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration
import java.util.concurrent.TimeUnit

@Configuration
class HttpClientConfig(
    private val properties: HttpClientProperties,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Bean
    fun okHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(Duration.ofSeconds(properties.connectTimeout))
            .readTimeout(Duration.ofSeconds(properties.readTimeout))
            .writeTimeout(Duration.ofSeconds(properties.writeTimeout))
            .connectionPool(
                ConnectionPool(
                    properties.maxIdleConnections,
                    properties.keepAliveDuration,
                    TimeUnit.MINUTES
                )
            )
            .retryOnConnectionFailure(properties.retryOnConnectionFailure)
            .followRedirects(true)
            .followSslRedirects(true)
            .addInterceptor(loggingInterceptor())
            .addInterceptor(headerInterceptor())
            .addInterceptor(errorHandlingInterceptor())
            .build()
    }

    // 로깅 인터셉터
    private fun loggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor { message ->
            logger.debug("OkHttp: $message")
        }.apply {
            level = when (properties.loggingLevel) {
                "NONE" -> HttpLoggingInterceptor.Level.NONE
                "BASIC" -> HttpLoggingInterceptor.Level.BASIC
                "HEADERS" -> HttpLoggingInterceptor.Level.HEADERS
                "BODY" -> HttpLoggingInterceptor.Level.BODY
                else -> HttpLoggingInterceptor.Level.BASIC
            }
        }
    }

    // 공통 헤더 인터셉터
    private fun headerInterceptor(): Interceptor {
        return Interceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("User-Agent", "Callula-Server/1.0")
                .addHeader("Accept", "application/json")
                .build()
            chain.proceed(request)
        }
    }

    // 에러 처리 인터셉터
    private fun errorHandlingInterceptor(): Interceptor {
        return Interceptor { chain ->
            val request = chain.request()
            try {
                val response = chain.proceed(request)

                // 응답 코드별 로깅
                when {
                    response.isSuccessful -> {
                        logger.debug("Success: ${request.url}")
                    }
                    response.code in 400..499 -> {
                        logger.warn("Client error ${response.code}: ${request.url}")
                    }
                    response.code in 500..599 -> {
                        logger.error("Server error ${response.code}: ${request.url}")
                    }
                }

                response
            } catch (e: Exception) {
                logger.error("Request failed: ${request.url}", e)
                throw e
            }
        }
    }
}
