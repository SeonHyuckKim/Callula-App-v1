package org.example.callulaserver.domains.callory.enums

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "식사 시간")
enum class MealTime(
    val displayName: String,
) {
    @Schema(description = "아침")
    BREAKFAST("아침"),

    @Schema(description = "점심")
    LUNCH("점심"),

    @Schema(description = "저녁")
    DINNER("저녁"),
}
