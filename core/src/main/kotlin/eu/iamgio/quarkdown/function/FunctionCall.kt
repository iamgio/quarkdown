package eu.iamgio.quarkdown.function

import eu.iamgio.quarkdown.function.expression.Expression
import eu.iamgio.quarkdown.function.value.InputValue
import eu.iamgio.quarkdown.function.value.OutputValue
import eu.iamgio.quarkdown.function.value.StringValue

/**
 * A call to a declared [Function].
 * This is an [Expression] as its output can be used as an input for another function call.
 * @param function referenced function to call
 * @param arguments arguments of the call
 * @param T expected output type of the function
 */
data class FunctionCall<T : OutputValue<*>>(
    val function: Function<T>,
    val arguments: List<FunctionCallArgument>,
) : Expression {
    /**
     * Checks the call validity and calls the function.
     * @return the function output
     */
    fun execute(): T {
        // Allows linking arguments to their parameter.
        val linker = FunctionArgumentsLinker(this)

        linker.link()
        return function.invoke(linker)
    }

    /**
     * When used as an input value for another function call, the output type of this function call
     * must be an [InputValue].
     */
    override fun eval(): InputValue<*> =
        this.execute().let {
            when (it) {
                is InputValue<*> -> it
                else -> {
                    // TODO error: not-input output value cannot be used as expression
                    StringValue("TODO")
                }
            }
        }

    override fun append(other: Expression): Expression =
        StringValue(this.eval().unwrappedValue.toString() + other.eval().unwrappedValue.toString())
}
