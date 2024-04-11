package eu.iamgio.quarkdown.function.call

import eu.iamgio.quarkdown.function.error.UnresolvedReferenceException
import eu.iamgio.quarkdown.function.expression.Expression
import eu.iamgio.quarkdown.function.expression.visitor.ExpressionVisitor
import eu.iamgio.quarkdown.function.value.OutputValue

/**
 * Wrapper/delegate for a [FunctionCall] whose referenced function may or may not have been resolved.
 * If [call] is `null`, the function wasn't resolved, and the delegate methods throw [UnresolvedReferenceException],
 * which is then caught by upper layers.
 * @param name name of the function call, shown in unresolved reference error messages
 * @param call optional call to wrap
 * @param T expected output type of the function
 */
data class UncheckedFunctionCall<T : OutputValue<*>>(val name: String, val call: FunctionCall<T>?) : Expression {
    /**
     * @return the result of `call.accept(visitor)` if [call] is not `null`
     * @throws UnresolvedReferenceException if [call] is `null`
     */
    override fun <T> accept(visitor: ExpressionVisitor<T>): T = call?.accept(visitor) ?: throw UnresolvedReferenceException(name)

    /**
     * @return the result of `call.execute()` if [call] is not `null`
     * @throws UnresolvedReferenceException if [call] is `null`
     */
    fun execute(): T = call?.execute() ?: throw UnresolvedReferenceException(name)
}
