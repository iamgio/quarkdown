package eu.iamgio.quarkdown.function.call

import eu.iamgio.quarkdown.context.Context
import eu.iamgio.quarkdown.function.Function
import eu.iamgio.quarkdown.function.call.binding.AllArgumentsBinder
import eu.iamgio.quarkdown.function.expression.Expression
import eu.iamgio.quarkdown.function.expression.visitor.ExpressionVisitor
import eu.iamgio.quarkdown.function.value.OutputValue

/**
 * A call to a declared [Function].
 * This is an [Expression] as its output can be used as an input for another function call.
 * @param function referenced function to call
 * @param arguments arguments of the call
 * @param context optional context this call lies in.
 *                This value can be injected to library functions that demand it via the `@Injected` annotation
 * @param T expected output type of the function
 */
data class FunctionCall<T : OutputValue<*>>(
    val function: Function<T>,
    val arguments: List<FunctionCallArgument>,
    val context: Context? = null,
) : Expression {
    /**
     * Checks the call validity and calls the function.
     * @return the function output
     */
    fun execute(): T {
        // Allows binding each argument to its parameter.
        val bindings = AllArgumentsBinder(this).createBindings(function.parameters)
        return function.invoke(bindings)
    }

    override fun <T> accept(visitor: ExpressionVisitor<T>): T = visitor.visit(this)
}
