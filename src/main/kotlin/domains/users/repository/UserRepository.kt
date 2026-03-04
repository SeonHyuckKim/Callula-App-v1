package org.example.callulaserver.domains.users.repository

import org.example.callulaserver.types.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface UserRepository : JpaRepository<User, UUID> {
    fun findByUuid(uuid: UUID): User?
    fun findByEmail(email: String): User? // ← 이것만 수정!
    fun findByUuidAndIsDeletedFalse(uuid: UUID): User?
    fun existsByEmail(email: String): Boolean
}
