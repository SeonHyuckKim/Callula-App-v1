package org.example.callulaserver.domains.callory.repository

import org.example.callulaserver.types.entity.Meal
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface MealRepository : JpaRepository<Meal, UUID> {
    fun findByUserUuidAndIsDeletedFalse(userUuid: UUID): List<Meal>
}
