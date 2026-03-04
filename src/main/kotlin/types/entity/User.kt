package org.example.callulaserver.types.entity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.example.callulaserver.types.enums.Gender
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "user")
class User(
    @Id
    @Column(name = "uuid", columnDefinition = "BINARY(16)")
    val uuid: UUID = UUID.randomUUID(),

    @Column(name = "email", nullable = false, unique = true, length = 100)
    var email: String,

    @Column(name = "password", nullable = false, length = 255)
    var password: String,

    @Column(name = "address", length = 255)
    var address: String? = null,

    @Column(name = "birth", nullable = false)
    var birth: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 10)
    var gender: Gender? = null,

    @Column(name = "height")
    var height: Double? = null,

    @Column(name = "weight")
    var weight: Double? = null,

    @Column(name = "phone", nullable = false, length = 100)
    var phone: String? = null,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @UpdateTimestamp
    @Column(name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "is_deleted", nullable = false)
    var isDeleted: Boolean = false,

//    @OneToMany(mappedBy = "user")
//    val accounts: List<Account> = mutableListOf()
) {
    // soft delete
    fun delete() {
        this.isDeleted = true
    }

    // 복구
    fun restore() {
        this.isDeleted = false
    }

    // BMR 계산
    fun calculateBmr(): Double {
        val h = height ?: throw IllegalStateException("키 정보가 없습니다.")
        val w = weight ?: throw IllegalStateException("몸무게 정보가 없습니다.")
        val g = gender ?: throw IllegalStateException("성별 정보가 없습니다.")
        val age = java.time.Period.between(java.time.LocalDate.parse(birth), java.time.LocalDate.now()).years

        return when (g) {
            Gender.M -> 66.5 + (13.75 * w) + (5.003 * h) - (6.75 * age)
            Gender.W -> 655.1 + (9.563 * w) + (1.850 * h) - (4.676 * age)
        }
    }
}
