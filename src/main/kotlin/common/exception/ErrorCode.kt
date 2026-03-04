package org.example.callulaserver.common.exception

interface CodeInterface {
    val code: Int
    var message: String
}

enum class ErrorCode(
    override val code: Int,
    override var message: String,
) : CodeInterface {
    AUTH_CONFIG_NOT_FOUND(-100, "auth config not found"),
    FAILED_TO_CALL_CLIENT(-101, "Failed to call client"),
    TOKEN_IS_INVALID(-102, "token is invalid error"),
    TOKEN_IS_EXPIRED(-105, "token is expired error"),
    FAIED_TO_SAVE_DATA(-107, "failed to save data"),
    CALL_RESULT_BODY_NULL(-108, "call result body is null"),
    USER_NOT_FOUND(-109, "user not found error"),
    USER_ALREADY_EXISTS(-110, "user already exist"),
    INVALID_PASSWORD(-111, "invalid password"),
    WITHDRAWAL_ALREADY_REQUESTED(-112, "withdrawal already requested"),
    WITHDRAWAL_NOT_FOUND(-113, "withdrawal not found"),
    WITHDRAWAL_REASON_DETAIL_REQUIRED(-114, "reason detail is required when reason is OTHER"),
    FAILED_TO_INVOKE_IN_LOGGER(-115, "failed to invoke in logger"),
    MEAL_NOT_FOUND(-116,"meal not found"),
    MEAL_ITEM_NOT_FOUND(-117,"meal item not found"),
}
