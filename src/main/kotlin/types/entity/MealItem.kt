package org.example.callulaserver.types.entity

import jakarta.persistence.*
import org.example.callulaserver.domains.callory.enums.FoodCategory
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "meal_item")
class MealItem(
    @Id
    @Column(name = "id", columnDefinition = "BINARY(16)")
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meal_id", nullable = false)
    val meal: Meal,

    @Column(name = "food_name", nullable = false, length = 100)
    var foodName: String,

    @Column(name = "calories")
    var calories: Int,

    @Enumerated(EnumType.STRING)
    @Column(name = "food_category", length = 20)
    var foodCategory: FoodCategory? = null,

    @Column(name = "image_url", length = 500)
    var imageUrl: String? = null,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @UpdateTimestamp
    @Column(name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now(),
)

