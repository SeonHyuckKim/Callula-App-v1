package org.example.callulaserver.domains.callory.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.example.callulaserver.domains.callory.dto.*
import org.example.callulaserver.domains.callory.enums.MealTime
import org.example.callulaserver.domains.callory.service.CalloryService
import org.example.callulaserver.types.dto.Response
import org.example.callulaserver.types.dto.ResponseProvider
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDate
import java.util.UUID

@Tag(name = "칼로리 API")
@RestController
@RequestMapping("/api/callory/v1")
class CalloryController(
    private val calloryService: CalloryService,
) {
    @Operation(summary = "유저가 먹은 식사 조회", description = "UUID로 유저가 먹은 식사를 조회합니다.")
    @GetMapping("/{userUuid}")
    fun getEatMealsByUser(
        @PathVariable
        @Parameter(
            name = "userUuid",
            description = "사용자 UUID",
            example = "98a26e57-2add-4e73-b5e8-e260b8df764a"
        )
        userUuid: UUID): Response<MealListResponse> {
        val meals = calloryService.getEatMealsByUser(userUuid)
        return ResponseProvider.success(meals)
    }

    @Operation(
        summary = "직접입력으로 식사 기록하기",
        description = "사용자가 직접 식사를 기입합니다."
    )
    @PostMapping("/{userUuid}/natural-language")
    fun directCreateMealByUser(
        @PathVariable
        @Parameter(
            name = "userUuid",
            description = "사용자 UUID",
            example = "98a26e57-2add-4e73-b5e8-e260b8df764a"  // ✅ 이 줄 추가
        )
        userUuid: UUID,
        @RequestBody request: CreateMealRequest,
    ): Response<CaloriesResponse> {
        val result = calloryService.directCreateMealByUser(userUuid, request)
        return ResponseProvider.success(result)
    }

    @Operation(
        summary = "사진으로 식사 기록하기",
        description = "사진을 업로드하면 Claude가 분석해서 저장합니다"
    )
    @PostMapping("/{userUuid}/image", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun createMealByImage(
        @PathVariable userUuid: UUID,
        @RequestParam mealTime: MealTime,
        @RequestParam image: MultipartFile,
        @RequestParam(required = false) mealDate: LocalDate? = null
    ): Response<CaloriesResponse> {
        val date = mealDate ?: LocalDate.now()
        val mediaType = image.contentType ?: "image/jpeg"

        val request = CreateMealByImageRequest(
            mealTime = mealTime,
            image = image
        )
        val result = calloryService.createMealByImage(userUuid, request, date,mediaType)
        return ResponseProvider.success(result)
    }

    @Operation(
        summary = "식사 기록 물리적 삭제",
        description = "식사 기록과 하위 음식 항목을 DB에서 완전히 삭제합니다."
    )
    @DeleteMapping("/{userUuid}/meals/{mealId}")
    fun deleteMeal(
        @PathVariable
        @Parameter(
            name = "userUuid",
            description = "사용자 UUID",
            example = "98a26e57-2add-4e73-b5e8-e260b8df764a"
        )
        userUuid: UUID,
        @PathVariable
        @Parameter(
            name = "mealId",
            description = "식사 UUID",
            example = "550e8400-e29b-41d4-a716-446655440000"
        )
        mealId: UUID,
    ): Response<Unit> {
        calloryService.deleteMeal(userUuid, mealId)
        return ResponseProvider.success()
    }

    @Operation(
        summary = "식사 기록 수정",
        description = "음식 항목 추가/수정/삭제가 가능합니다."
    )
    @PatchMapping("/{userUuid}/meals/{mealId}")
    fun updateMeal(
        @PathVariable
        @Parameter(
            name = "userUuid",
            description = "사용자 UUID",
            example = "98a26e57-2add-4e73-b5e8-e260b8df764a"
        )
        userUuid: UUID,
        @PathVariable
        @Parameter(
            name = "mealId",
            description = "식사 UUID",
            example = "550e8400-e29b-41d4-a716-446655440000"
        )
        mealId: UUID,
        @RequestBody request: UpdateMealRequest,
    ): Response<MealResponse> {
        val result = calloryService.updateMeal(userUuid,
            mealId, request)
        return ResponseProvider.success(result)
    }
}
