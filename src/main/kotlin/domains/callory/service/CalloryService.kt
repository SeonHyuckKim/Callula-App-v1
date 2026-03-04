package org.example.callulaserver.domains.callory.service

import org.example.callulaserver.common.exception.CustomException
import org.example.callulaserver.common.exception.ErrorCode
import org.example.callulaserver.common.logging.Logging
import org.example.callulaserver.common.transaction.Transactional
import org.example.callulaserver.domains.callory.dto.*
import org.example.callulaserver.domains.callory.repository.MealRepository
import org.example.callulaserver.domains.claude.service.ClaudeService
import org.example.callulaserver.domains.users.repository.UserRepository
import org.example.callulaserver.types.entity.Meal
import org.example.callulaserver.types.entity.MealItem
import org.slf4j.Logger
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.util.*

@Service
class CalloryService(
    private val claudeService: ClaudeService,
    private val mealRepository: MealRepository,
    private val userRepository: UserRepository,
    private val transaction: Transactional,
    private val logger: Logger = Logging.getLogger(CalloryService::class.java),
) {
    fun getEatMealsByUser(userUuid: UUID): MealListResponse =
        Logging.logFor(logger) { log ->
            transaction.readOnly {
                log["action"] = "getEatMealsByUser"
                log["user_uuid"] = userUuid

                val meals = mealRepository.findByUserUuidAndIsDeletedFalse(userUuid)

                log["meal_count"] = meals.size
                log["status"] = "success"

                MealListResponse.from(meals)
            }
        }

    fun directCreateMealByUser(userUuid: UUID, request: CreateMealRequest): CaloriesResponse =
        Logging.logFor(logger) { log ->
            transaction.run {
                log["action"] = "directCreateMealByUser"
                log["user_uuid"] = userUuid
                log["food_name"] = request.items.map { it.foodName }


                // 사용자 조회
                val user = userRepository.findByUuidAndIsDeletedFalse(userUuid)
                    ?: throw CustomException(
                        codeInterface = ErrorCode.USER_NOT_FOUND,
                        additionalMessage = "uuid: $userUuid"
                    )

                val meal = Meal(
                    user = user,
                    mealDate = request.mealDate,
                    mealTime = request.mealTime,
                    memo = request.memo
                )
                // 칼로리 없는 항목만 AI 에게 요청
                val itemsNeedEstimation = request.items.filter { it.calories == null }
                val itemsWithCalories = request.items.filter { it.calories != null }

                //칼로리 있는 항목은 그대로 저장
                itemsWithCalories.forEach { item ->
                    meal.addItem(
                        MealItem(
                            meal = meal,
                            foodName = item.foodName,
                            foodCategory = item.foodCategory,
                            calories = item.calories!!
                        )
                    )
                }
                //칼로리 없는 항목만 AI 추정
                if (itemsNeedEstimation.isNotEmpty()) {
                    val description = itemsNeedEstimation.map { it.foodName }.joinToString(", ")
                    val analyzed = claudeService.analyzeMealFromNaturalLanguage(description)

                    analyzed.items.forEach { item ->
                        meal.addItem(
                            MealItem(
                                meal = meal,
                                foodName = item.foodName,
                                foodCategory = item.foodCategory,
                                calories = item.calories
                            )
                        )
                    }
                }

                mealRepository.save(meal) // Cascade 되어있어서 meal 저장시 mealItem도 DB에 저장

                log["status"] = "success"
                log["total_calories"] = meal.totalCalories


                CaloriesResponse(totalCalories = meal.totalCalories)
            }
        }

    fun createMealByImage(
        userUuid: UUID,
        request: CreateMealByImageRequest,
        mealDate: LocalDate,
        mediaType: String,
    ): CaloriesResponse =
        Logging.logFor(logger) { log ->
            transaction.run {
                log["action"] = "createMealByImage"
                log["user_uuid"] = userUuid
                log["meal_time"] = request.mealTime

                // 사용자 조회
                val user = userRepository.findByUuidAndIsDeletedFalse(userUuid)
                    ?: throw CustomException(
                        codeInterface = ErrorCode.USER_NOT_FOUND,
                        additionalMessage = "uuid: $userUuid"
                    )

                val imageBytes = request.image.bytes
                val imageBase64 = Base64.getEncoder().encodeToString(imageBytes)

                val analyzedMeal = claudeService.analyzeMealFromImage(imageBase64, mediaType)
                log["food_names"] = analyzedMeal.items.map { it.foodName }
                log["food_category"] = analyzedMeal.items.map { it.foodCategory }

                // Meal 엔티티 생성
                val meal = Meal(
                    user = user,
                    mealDate = mealDate,
                    mealTime = request.mealTime,
                    memo = "사진으로 분석됨"
                )

                analyzedMeal.items.forEach { item ->
                    meal.addItem(
                        MealItem(
                            meal = meal,
                            foodName = item.foodName,
                            foodCategory = item.foodCategory,
                            calories = item.calories
                        )
                    )
                }

                mealRepository.save(meal)

                log["status"] = "success"
                log["total_calories"] = meal.totalCalories

                CaloriesResponse(totalCalories = meal.totalCalories)
            }
        }

    fun updateMeal(
        userUuid: UUID,
        mealId: UUID,
        request: UpdateMealRequest,
    ): MealResponse =
        Logging.logFor(logger) { log ->
            transaction.run {
                log["action"] = "updateMeal"
                log["user_uuid"] = userUuid
                log["meal_id"] = mealId

                val meal =
                    mealRepository.findById(mealId).orElseThrow {
                        CustomException(
                            codeInterface =
                                ErrorCode.MEAL_NOT_FOUND,
                            additionalMessage = "mealId: $mealId"
                        )
                    }

                // 소유자 검증
                if (meal.user.uuid != userUuid) {
                    throw CustomException(
                        codeInterface =
                            ErrorCode.MEAL_NOT_FOUND,
                        additionalMessage = "meal does not belong to user : $userUuid"
                    )
                }

                // 삭제된 식사인지 확인
                if (meal.isDeleted) {
                    throw CustomException(
                        codeInterface =
                            ErrorCode.MEAL_NOT_FOUND,
                        additionalMessage = "meal is deleted: $mealId"
                    )
                }

                // 1. MealItem 삭제
                request.deleteItemIds?.forEach { itemId ->
                    val item = meal.items.find {
                        it.id == itemId
                    }
                        ?: throw CustomException(
                            codeInterface =
                                ErrorCode.MEAL_ITEM_NOT_FOUND,
                            additionalMessage = "mealItemId: $itemId"
                    )
                    meal.removeItem(item)
                }

                // 2. 기존 MealItem 수정
                request.updateItems?.forEach { updateReq ->
                    val item = meal.items.find {
                        it.id == updateReq.id
                    }
                        ?: throw CustomException(
                            codeInterface =
                                ErrorCode.MEAL_ITEM_NOT_FOUND,
                            additionalMessage = "mealItemId: ${ updateReq.id }"
                    )
                    updateReq.foodName?.let {
                        item.foodName = it
                    }
                    updateReq.calories?.let {
                        item.calories = it
                    }
                    updateReq.foodCategory?.let {
                        item.foodCategory = it
                    }
                }

                // 3. 새 MealItem 추가
                request.addItems?.forEach { addReq ->
                    meal.addItem(
                        MealItem(
                            meal = meal,
                            foodName = addReq.foodName,
                            foodCategory = addReq.foodCategory,
                            calories = addReq.calories ?: 0
                        )
                    )
                }

                log["status"] = "success"
                log["total_calories"] = meal.totalCalories

                MealResponse.from(meal)
            }
        }

    fun deleteMeal(
        userUuid: UUID,
        mealId: UUID,
    ): Unit =
        Logging.logFor(logger) { log ->
            transaction.run {
                log["action"] = "deleteMeal"
                log["user_uuid"] = userUuid
                log["meal_id"] = mealId

                val meal = mealRepository.findById(mealId).orElseThrow {
                    CustomException(
                        codeInterface = ErrorCode.MEAL_NOT_FOUND,
                        additionalMessage = "mealId: $mealId"
                    )
                }

                if (meal.user.uuid != userUuid) {
                    throw CustomException(
                        codeInterface = ErrorCode.MEAL_NOT_FOUND,
                        additionalMessage = "meal does not belong to user: $userUuid"
                    )
                }

                log["meal_item_ids"] = meal.items.map { it.foodName }

                mealRepository.delete(meal)

                log["status"] = "success"
            }
        }


}
