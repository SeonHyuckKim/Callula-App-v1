package org.example.callulaserver.domains.users.dto

import io.swagger.v3.oas.annotations.media.Schema
import org.example.callulaserver.types.entity.User
import org.example.callulaserver.types.enums.Gender
import java.time.LocalDateTime
import java.util.*

@Schema
data class CreateUserRequest(
    @Schema(
        description = "이메일 주소",
        example = "test@naver.com",
        required = true
    )
    val email: String,
    @Schema(
        description = "비밀번호",
        example = "qweasd455!@",
        required = true
    )
    val password: String,
    @Schema(
        description = "생년월일",
        example = "1993-05-31",
        required = true
    )
    val birth: String,
    @Schema(
        description = "주소",
        example = "경기도 군포시 군포시청",
        required = true
    )
    val address: String? = null,
    @Schema(
        description = "성별 남자: M 여자 W",
        example = "M",
        required = true
    )
    val gender: Gender? = null,

    @Schema(
        description = "키 (cm)",
        example = "175.0",
        required = true
    )
    val height: Double? = null,

    @Schema(
        description = "몸무게 (kg)",
        example = "65.7",
        required = true
    )
    val weight: Double? = null,

    @Schema(
        description = "휴대폰 연락처",
        example = "01012340000",
        required = true
    )
    val phone: String? = null,
)

data class UpdateUserRequest(
    @Schema(
        description = "주소 수정",
        example = "경기도 성남시 분당구"
    )
    val address: String? = null,
    val gender: Gender? = null,
    @Schema(
        description = "휴대폰 연락처 수정",
        example = "01032324343"
    )
    val phone: String? = null,
    @Schema(
        description = "키 수정 (cm)",
        example = "174.5"
    )
    val height: Double? = null,
    @Schema(
        description = "몸무게 수정 (kg)",
        example = "70.1"
    )
    val weight: Double? = null,
)

data class UserResponse(
    val uuid: UUID,
    val email: String,
    val address: String?,
    val birth: String,
    val gender: Gender?,
    val height: Double?,
    val weight: Double?,
    val phone: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val bmr: Double?,
) {
    companion object {
        fun from(user: User): UserResponse { // Entity User 사용
            return UserResponse(
                uuid = user.uuid,
                email = user.email,
                address = user.address,
                birth = user.birth,
                gender = user.gender,
                height = user.height,
                weight = user.weight,
                phone = user.phone,
                createdAt = user.createdAt,
                updatedAt = user.updatedAt,
                bmr = if (user.height != null && user.weight != null && user.gender != null) {
                    user.calculateBmr()
                } else {
                    null
                }
            )
        }
    }
}
