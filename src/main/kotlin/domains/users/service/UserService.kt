package org.example.callulaserver.domains.users.service

import org.example.callulaserver.common.exception.CustomException
import org.example.callulaserver.common.exception.ErrorCode
import org.example.callulaserver.common.logging.Logging
import org.example.callulaserver.common.transaction.Transactional
import org.example.callulaserver.domains.users.dto.CreateUserRequest
import org.example.callulaserver.domains.users.dto.UpdateUserRequest
import org.example.callulaserver.domains.users.dto.UserResponse
import org.example.callulaserver.domains.users.repository.UserRepository
import org.example.callulaserver.types.entity.User
import org.slf4j.Logger
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.util.*

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val transaction: Transactional,
    private val logger: Logger = Logging.getLogger(UserService::class.java),
) {
    fun createUser(request: CreateUserRequest): UserResponse = Logging.logFor(logger) { log ->
        transaction.run {
            validateDuplicateEmail(request.email)

            val encodedPassword = passwordEncoder.encode(request.password)

            val user = User(
                email = request.email,
                password = encodedPassword,
                birth = request.birth,
                address = request.address,
                gender = request.gender,
                phone = request.phone,
                height = request.height,
                weight = request.weight
            )

            log["action"] = "createUser"
            log["email"] = request.email

            val savedUser = userRepository.save(user)

            log["user_uuid"] = savedUser.uuid
            log["status"] = "success"

            UserResponse.from(savedUser)
        }
    }

    fun updateUser(uuid: UUID, request: UpdateUserRequest): UserResponse = Logging.logFor(logger) { log ->
        transaction.run {
            log["action"] = "updateUser"
            log["user_uuid"] = uuid

            val user = findUserByUuid(uuid)

            request.address?.let {
                log["before_address"] = user.address ?: "null"
                user.address = it
                log["after_address"] = it
            }

            request.gender?.let {
                log["before_gender"] = user.gender ?: "null"
                user.gender = it
                log["after_gender"] = it
            }

            request.phone?.let {
                log["before_phone"] = user.phone ?: "null"
                user.phone = it
                log["after_phone"] = it
            }

            request.height?.let {
                log["before_height"] = user.height ?: "null"
                user.height = it
                log["after_height"] = it
            }

            request.weight?.let {
                log["before_weight"] = user.weight ?: "null"
                user.weight = it
                log["after_weight"] = it
            }

            log["status"] = "success"

            UserResponse.from(user)
        }
    }

    fun getUserInfo(uuid: UUID): UserResponse = Logging.logFor(logger) { log ->
        transaction.readOnly {
            log["action"] = "getUserInfo"
            log["user_uuid"] = uuid

            val user = findUserByUuid(uuid)

            log["email"] = user.email
            log["status"] = "success"

            UserResponse.from(user)
        }
    }

    private fun findUserByUuid(uuid: UUID): User {
        return userRepository.findByUuidAndIsDeletedFalse(uuid)
            ?: throw CustomException(
                codeInterface = ErrorCode.USER_NOT_FOUND,
                additionalMessage = "uuid: $uuid"
            )
    }

    private fun validateDuplicateEmail(email: String) {
        if (userRepository.existsByEmail(email)) {
            throw CustomException(
                codeInterface = ErrorCode.USER_ALREADY_EXISTS,
                additionalMessage = "email: $email"
            )
        }
    }
}
