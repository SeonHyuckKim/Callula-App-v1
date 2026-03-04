package org.example.callulaserver.domains.callory.dto

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import org.example.callulaserver.domains.callory.enums.FoodCategory
import org.example.callulaserver.domains.callory.enums.MealTime
import org.example.callulaserver.types.entity.Meal
import org.example.callulaserver.types.entity.MealItem
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID



@Schema(description = "음식 기록 응답")
data class MealItemResponse(
    @Schema(description = "음식 기록 UUID", example = "550e8400-e29b-41d4-a716-446655440000")
    val id: UUID,
    @Schema(description = "음식 이름", example = "바나나")
    val foodName: String,
    @Schema(description = "음식 카테고리", example = "JAPANESE")
    val foodCategory: FoodCategory?,
    @Schema(description = "음식 칼로리", example = "150")
    val calories: Int,
) {
    companion object {
        fun from(mealItem: MealItem): MealItemResponse = MealItemResponse(
            id = mealItem.id,
            foodName = mealItem.foodName,
            foodCategory = mealItem.foodCategory?:FoodCategory.OTHER,
            calories = mealItem.calories
        )
    }
}


@Schema(description = "식사 기록 응답")
data class MealResponse(
    @Schema(description = "식사 기록 UUID", example = "550e8400-e29b-41d4-a716-446655440000")
    val id: UUID,

    @Schema(description = "사용자 UUID", example = "550e8400-e29b-41d4-a716-446655440001")
    val userUuid: UUID,

    @Schema(description = "식사 날짜", example = "2026-02-16")
    val mealDate: LocalDate,

    @Schema(description = "식사 시간", example = "BREAKFAST")
    val mealTime: MealTime,

    val items: List<MealItemResponse>,

    val totalCalories: Int,

    @Schema(description = "메모", example = "정말 맛있었습니다")
    val memo: String?,

    @Schema(description = "생성일", example = "2026-02-16T12:00:00")
    val createdAt: LocalDateTime,

    @Schema(description = "수정일", example = "2026-02-16T14:00:00")
    val updatedAt: LocalDateTime,
) {
    companion object {
        fun from(meal: Meal): MealResponse = MealResponse(
            id = meal.id,
            userUuid = meal.user.uuid,
            mealDate = meal.mealDate,
            mealTime = meal.mealTime,
            items = meal.items.map { MealItemResponse.from(it) },
            totalCalories = meal.totalCalories,
            memo = meal.memo,
            createdAt = meal.createdAt,
            updatedAt = meal.updatedAt
        )
    }
}

@Schema(description = "식사 기록 목록 응답")
data class MealListResponse(
    @Schema(description = "식사 기록 목록")
    val meals: List<MealResponse>,

    @Schema(description = "전체 개수", example = "5")
    val total: Int,

    @Schema(description = "조회 날짜", example = "2026-02-16")
    val date: LocalDate? = null,
) {
    companion object {
        fun from(meals: List<Meal>, date: LocalDate? = null): MealListResponse {
            return MealListResponse(
                meals = meals.map { MealResponse.from(it) },
                total = meals.size,
                date = date
            )
        }
    }
}

@Schema(description = "식사 기록 요청 (자연어)")
data class MealNaturalLanguageRequest(
    @Schema(description = "사용자 입력 (예: '오늘 아침 김밥 먹었어, 칼로리는 500')", example = "점심에 햄버거 먹었어")
    val userInput: String,

    @Schema(description = "식사 날짜", example = "2026-02-22")
    val mealDate: LocalDate,

    @Schema(description = "식사 시간", example = "BREAKFAST")
    val mealTime: MealTime,

    @Schema(description = "입력 타입", example = "DIR | PHOTO")
    val inputType: String,
)

@Schema(description = "Claude가 분석한 개별 음식 정보")
data class MealAnalyzedItem(
    @Schema(description = "음식 이름", example = "김밥")
    val foodName: String = "",

    @Schema(description = "음식 카테고리", example = "KOREAN")
    val foodCategory: FoodCategory = FoodCategory.OTHER,

    @Schema(description = "칼로리", example = "450")
    val calories: Int = 0,

    @Schema(description = "메모", example = "김밥 한줄")
    val memo: String? = null,
)

@Schema(description = "Claude가 분석한 식사 정보 (복수 음식)")
data class MealAnalyzedResponse(
    @Schema(description = "분석된 음식 목록")
    val items: List<MealAnalyzedItem> = emptyList(),
)

@Schema(description = "식사 기록 응답 (간단)")
data class MealNaturalLanguageResponse(

    @Schema(description = "식사 시간", example = "BREAKFAST")
    val mealTime: MealTime,

    @Schema(description = "식사 섭취한 음식들", example = "바나나, 우유 , 콘푸로스트")
    val items: List<MealItemResponse>,

    @Schema(description = "총 섭취 칼로리", example = "480")
    val totalCalories: Int,
    ) {
    companion object {
        fun from(meal: Meal): MealNaturalLanguageResponse = MealNaturalLanguageResponse(
            items = meal.items.map { MealItemResponse.from(it) },
            totalCalories = meal.totalCalories,
            mealTime = meal.mealTime,

        )
    }
}

@Schema(description = "단일 음식 생성 요청(*사용자 직접 입력*)")
data class MealItemRequest(
    @Schema(description = "음식 이름")
    val foodName: String,
    @Schema(description = "음식 카테고리")
    val foodCategory: FoodCategory?,
    @Schema(description = "음식 칼로리")
    val calories: Int?,
)

@Schema(description = "식사 기록 생성 요청")
data class CreateMealRequest(
    @Schema(description = "식사 날짜", example = "2026-02-16")
    val mealDate: LocalDate,

    @Schema(description = "식사 시간", example = "BREAKFAST")
    val mealTime: MealTime,

    @Schema(description = "먹은 음식들")
    val items: List<MealItemRequest>,

    @Schema(description = "메모", example = "정말 맛있었습니다")
    val memo: String?,
)

@Schema(description = "칼로리만 반환하는 응답")
data class CaloriesResponse(
    @Schema(description = "총 칼로리", example = "500")
    val totalCalories: Int,
) {
    companion object {
        fun from(meal: Meal): CaloriesResponse = CaloriesResponse(
            totalCalories = meal.totalCalories
        )
    }
}

@Schema(description = "사진으로 식사 기록 요청")
data class CreateMealByImageRequest(
    @Schema(description = "식사 시간", example = "BREAKFAST")
    val mealTime: MealTime,

    @Schema(description = "식사 사진")
    val image: MultipartFile
)

@Schema(description = "Claude가 분석한 개별 음식 정보")
data class MealImageAnalyzedItem(
    @JsonProperty("foodName")
    @Schema(description = "음식 이름", example = "라멘")
    val foodName: String = "",

    @JsonProperty("foodCategory")
    @Schema(description = "음식 카테고리", example = "JAPANESE")
    val foodCategory: FoodCategory = FoodCategory.OTHER,

    @JsonProperty("calories")
    @Schema(description = "칼로리", example = "550")
    val calories: Int = 0
)

@Schema(description = "Claude가 분석한 식사 정보 (복수 음식)")
data class MealImageAnalyzedResponse(
    @JsonProperty("items")
    @Schema(description = "분석된 음식 목록")
    val items: List<MealImageAnalyzedItem> = emptyList()
)

@Schema(description = "기존 음식 항목 수정 요청")
data class UpdateMealItemRequest(
    @Schema(description = "수정할 MealItem UUID", example =
        "550e8400-e29b-41d4-a716-446655440000")
    val id: UUID,

    @Schema(description = "음식 이름", example = "된장찌개")
    val foodName: String? = null,

    @Schema(description = "칼로리", example = "300")
    val calories: Int? = null,

    @Schema(description = "음식 카테고리", example = "KOREAN")
    val foodCategory: FoodCategory? = null,
)

@Schema(description = "식사 수정 요청")
data class UpdateMealRequest(
    @Schema(description = "기존 음식 항목 수정")
    val updateItems: List<UpdateMealItemRequest>? = null,

    @Schema(description = "새로운 음식 항목 추가")
    val addItems: List<MealItemRequest>? = null,

    @Schema(description = "삭제할 음식 항목 UUID 목록")
    val deleteItemIds: List<UUID>? = null,
)



