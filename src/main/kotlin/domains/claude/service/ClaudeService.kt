package org.example.callulaserver.domains.claude.service

import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.example.callulaserver.domains.callory.dto.MealAnalyzedResponse
import org.example.callulaserver.domains.callory.dto.MealImageAnalyzedResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class ClaudeService(
    @Value("\${anthropic.api-key}")
    private val apiKey: String,
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val httpClient = OkHttpClient()
    private val objectMapper = ObjectMapper()

    fun analyzeMealFromNaturalLanguage(userInput: String): MealAnalyzedResponse {
        try {
            val prompt = """
                사용자가 다음과 같이 식사를 설명했습니다:
                "$userInput"

                여러 음식이 포함될 수 있습니다. 분석해서 JSON 형식으로만 반환해주세요.

                {
                  "items": [
                    {
                      "foodName": "음식 이름",
                      "foodCategory": "KOREAN|CHINESE|JAPANESE|WESTERN|FAST_FOOD|DESSERT|BEVERAGE|SNACK|OTHER",
                      "calories": 숫자,
                      "memo": "메모 또는 null"
                    }
                  ]
                }
            """.trimIndent()

            val params = mapOf(
                "model" to "claude-opus-4-20250514",
                "max_tokens" to 1024,
                "messages" to listOf(
                    mapOf(
                        "role" to "user",
                        "content" to prompt
                    )
                )
            )

            val requestJson = objectMapper.writeValueAsString(params)

            val request = Request.Builder()
                .url("https://api.anthropic.com/v1/messages")
                .post(requestJson.toRequestBody("application/json".toMediaType()))
                .addHeader("x-api-key", apiKey)
                .addHeader("anthropic-version", "2023-06-01")
                .build()

            val response = httpClient.newCall(request).execute()

            if (!response.isSuccessful) {
                throw Exception("Claude API error: ${response.code}")
            }

            val responseBody = response.body?.string() ?: throw Exception("Empty response")
            logger.info("Claude response: $responseBody")

            val responseJson = objectMapper.readTree(responseBody)
            val textContent = responseJson.get("content")?.get(0)?.get("text")?.asText()
                ?: throw Exception("No text content in response")

            logger.info("Text content: $textContent")

            val cleanedText = textContent
                .replace("```json", "")
                .replace("```", "")
                .trim()

            val jsonMatch = Regex("\\{[\\s\\S]*\\}").find(cleanedText)
            val jsonString = jsonMatch?.value ?: throw Exception("No JSON found in response")

            val analyzedData = objectMapper.readValue(jsonString, MealAnalyzedResponse::class.java)

            logger.info("Analyzed meal: $analyzedData")
            return analyzedData
        } catch (e: Exception) {
            logger.error("Claude API error: ${e.message}")
            throw Exception("식사 분석 실패: ${e.message}")
        }
    }

    fun analyzeMealFromImage(imageBase64: String,mediaType: String): MealImageAnalyzedResponse {
        try {
            val supportedFormats = listOf("image/jpeg", "image/png", "image/gif", "image/webp")
            if (!supportedFormats.contains(mediaType)) {
                throw Exception("지원하지 않는 이미지 형식: $mediaType")
            }

            val prompt = """
            이 사진에 있는 음식을 모두 분석해주세요.
            여러 음식이 있을 수 있습니다.

            다음 형식의 JSON으로만 반환해주세요:
            {
              "items": [
                {
                  "foodName": "음식 이름 (한글)",
                  "foodCategory": "KOREAN|CHINESE|JAPANESE|WESTERN|FAST_FOOD|DESSERT|BEVERAGE|SNACK|OTHER 중 하나",
                  "calories": 추정 칼로리 (숫자)
                }
              ]
            }
        """.trimIndent()

            val params = mapOf(
                "model" to "claude-opus-4-20250514",
                "max_tokens" to 1024,
                "messages" to listOf(
                    mapOf(
                        "role" to "user",
                        "content" to listOf(
                            mapOf(
                                "type" to "image",
                                "source" to mapOf(
                                    "type" to "base64",
                                    "media_type" to mediaType,
                                    "data" to imageBase64
                                )
                            ),
                            mapOf(
                                "type" to "text",
                                "text" to prompt
                            )
                        )
                    )
                )
            )

            val requestJson = objectMapper.writeValueAsString(params)

            val request = Request.Builder()
                .url("https://api.anthropic.com/v1/messages")
                .post(requestJson.toRequestBody("application/json".toMediaType()))
                .addHeader("x-api-key", apiKey)
                .addHeader("anthropic-version", "2023-06-01")
                .build()

            val response = httpClient.newCall(request).execute()

            if (!response.isSuccessful) {
                throw Exception("Claude API error: ${response.code}")
            }

            val responseBody = response.body?.string() ?: throw Exception("Empty response")
            logger.info("Claude response: $responseBody")

            val responseJson = objectMapper.readTree(responseBody)
            val textContent = responseJson.get("content")?.get(0)?.get("text")?.asText()
                ?: throw Exception("No text content in response")

            // ✅ 백틱 제거 및 JSON 추출
            val cleanedText = textContent
                .replace("```json", "")
                .replace("```", "")
                .trim()

            // JSON 파싱 (중첩 JSON 지원)
            val jsonMatch = Regex("\\{[\\s\\S]*\\}").find(cleanedText)
            val jsonString = jsonMatch?.value ?: throw Exception("No JSON found in response")

            logger.info("Cleaned JSON: $jsonString")

            val analyzedData = objectMapper.readValue(jsonString, MealImageAnalyzedResponse::class.java)

            logger.info("Analyzed meal from image: $analyzedData")
            return analyzedData

        } catch (e: Exception) {
            logger.error("Claude image analysis error: ${e.message}")
            throw Exception("식사 사진 분석 실패: ${e.message}")
        }
    }
}
