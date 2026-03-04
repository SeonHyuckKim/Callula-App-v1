package org.example.callulaserver.common.transaction
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

// self-invocation으로 인해 트랜잭션 별도로 생성 같은 클래스내 AOP 한계
interface Runner {
    fun <T> run(function: () -> T?): T?
    fun <T> readOnly(function: () -> T?): T?
}

@Component
class TransactionalAdvice : Runner {
    @Transactional
    override fun <T> run(function: () -> T?): T? = function()

    @Transactional(readOnly = true)
    override fun <T> readOnly(function: () -> T?): T? = function()
}

@Component
class Transactional(
    private val advice: Runner,
) {
    fun <T> run(function: () -> T?): T? = advice.run(function)

    fun <T> readOnly(function: () -> T?): T? = advice.readOnly(function)
}
