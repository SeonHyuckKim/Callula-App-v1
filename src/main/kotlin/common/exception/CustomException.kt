package org.example.callulaserver.common.exception

class CustomException(
    val codeInterface: CodeInterface,
    val additionalMessage: String? = null,
) : RuntimeException(
    if (additionalMessage == null) {
        codeInterface.message
    } else {
        "${codeInterface.message} - $additionalMessage"
    }
) {
    fun getCode(): Int = codeInterface.code

    fun getFullMessage(): String = message ?: codeInterface.message
}
