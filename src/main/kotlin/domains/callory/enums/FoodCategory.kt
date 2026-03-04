package org.example.callulaserver.domains.callory.enums

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "음식 카테고리")
enum class FoodCategory(
    val displayName: String,
) {
    @Schema(description = "한식")
    KOREAN("한식"),

    @Schema(description = "중식")
    CHINESE("중식"),

    @Schema(description = "일식")
    JAPANESE("일식"),

    @Schema(description = "양식")
    WESTERN("양식"),

    @Schema(description = "분식")
    SNACK("분식"),

    @Schema(description = "패스트푸드")
    FAST_FOOD("패스트푸드"),

    @Schema(description = "디저트")
    DESSERT("디저트"),

    @Schema(description = "음료")
    BEVERAGE("음료"),

    @Schema(description = "기타")
    OTHER("기타"),
}
