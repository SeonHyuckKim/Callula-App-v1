package org.example.callulaserver.types.entity

import jakarta.persistence.*
import org.example.callulaserver.domains.callory.enums.MealTime
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "meal")
class Meal(
    @Id
    @Column(name = "id", columnDefinition = "BINARY(16)")
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_uuid", nullable = false)
    val user: User,

    @Column(name = "meal_date", nullable = false)
    val mealDate: LocalDate,

    @Enumerated(EnumType.STRING)
    @Column(name = "meal_time", nullable = false, length = 20)
    val mealTime: MealTime, // BREAKFAST, LUNCH, DINNER


    @Column(name = "memo", length = 500)
    var memo: String? = null,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @UpdateTimestamp
    @Column(name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "is_deleted", nullable = false)
    var isDeleted: Boolean = false,

    ) {
    @OneToMany(mappedBy = "meal", fetch = FetchType.LAZY, cascade =
        [CascadeType.ALL], orphanRemoval = true)
    val items: MutableList<MealItem> = mutableListOf()
    // meal 에서 meal_item 총 칼로리 계산하려면 meal_item list 를 direct로 알아야함

    fun addItem(item: MealItem){
        items.add(item)
    }
    fun removeItem(item: MealItem){
        items.remove(item)
    }
    val totalCalories: Int
        get() = items.sumOf { it.calories ?: 0 }

    fun delete() {
        this.isDeleted = true
    }

    fun restore() {
        this.isDeleted = false
    }
}
