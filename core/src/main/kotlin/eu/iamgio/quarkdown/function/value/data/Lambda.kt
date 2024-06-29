package eu.iamgio.quarkdown.function.value.data

import eu.iamgio.quarkdown.context.Context
import eu.iamgio.quarkdown.function.SimpleFunction
import eu.iamgio.quarkdown.function.error.InvalidLambdaArgumentCountException
import eu.iamgio.quarkdown.function.expression.eval
import eu.iamgio.quarkdown.function.library.Library
import eu.iamgio.quarkdown.function.reflect.DynamicValueConverter
import eu.iamgio.quarkdown.function.reflect.FromDynamicType
import eu.iamgio.quarkdown.function.value.DynamicValue
import eu.iamgio.quarkdown.function.value.OutputValue
import eu.iamgio.quarkdown.function.value.Value
import eu.iamgio.quarkdown.function.value.ValueFactory

private const val LAMBDA_LIBRARY_NAME = "__lambda-parameters__"

/**
 * An action block with a variable parameter count.
 * The return type is dynamic (a snippet of raw Quarkdown code is returned), hence it is evaluated and converted to a static type.
 * @param parentContext context this lambda lies in
 * @param explicitParameters named parameters of the lambda. If not present, parameter names are automatically set to .1, .2, etc.
 * @param action action to perform, which takes a variable sequence of [Value]s as arguments and returns a Quarkdown code snippet.
 */
class Lambda(
    val parentContext: Context,
    val explicitParameters: List<String> = emptyList(),
    val action: (Array<out Value<*>>) -> String,
) {
    /**
     * Registers the arguments in the context, which can be accessed as function calls.
     * @param arguments arguments of the lambda action
     */
    private fun createLambdaParametersLibrary(vararg arguments: Value<*>) =
        Library(
            LAMBDA_LIBRARY_NAME,
            functions =
                arguments.mapIndexed { index, argument ->
                    val parameterName = explicitParameters.getOrNull(index) ?: (index + 1).toString()
                    SimpleFunction(
                        parameterName,
                        parameters = emptyList(),
                    ) {
                        // Value associated to the lambda argument.
                        DynamicValue(argument.unwrappedValue)
                    }
                }.toSet(),
        )

    /**
     * Invokes the lambda action with given arguments.
     * @param arguments arguments of the lambda action
     * @return the result of the lambda action, as an undetermined, thus dynamically-typed, value
     */
    fun invokeDynamic(vararg arguments: Value<*>): OutputValue<*> {
        // Check if the amount of arguments matches the amount of expected parameters.
        // In case parameters are not present, placeholders are automatically set to
        // .1, .2, etc., similarly to Kotlin's 'it' argument.
        // This replacement is handled by ValueFactory.lambda
        if (arguments.size != explicitParameters.size && explicitParameters.isNotEmpty()) {
            throw InvalidLambdaArgumentCountException(explicitParameters.size, arguments.size)
        }

        // Create a new independent context, copy of the parent one, to execute the lambda block in.
        // Upon invocation, the context is filled with the arguments information,
        // whose values can be retrieved as function calls.
        val context = parentContext.fork()

        // Register the arguments in the context, which can be accessed as function calls.
        context.libraries += createLambdaParametersLibrary(*arguments)

        val output =
            ValueFactory.expression(action(arguments), context)?.eval()
                ?: throw IllegalArgumentException("Cannot invoke dynamically-typed lambda: null result")

        return when (output) {
            is OutputValue<*> -> output
            else -> DynamicValue(output)
        }
    }

    /**
     * Invokes the lambda action with given arguments and converts it to a static type.
     * @param values arguments of the lambda action
     * @param T **unwrapped** type to convert the resulting dynamic value to.
     * This type must appear in a [FromDynamicType] annotation on a [ValueFactory] method
     * @param V **wrapped** value type (which wraps [T]) to convert the resulting dynamic value to
     * @return the result of the lambda action, as a statically typed value
     */
    inline fun <reified T, reified V : Value<T>> invoke(vararg values: Value<*>): V {
        // Invoke the lambda action and convert the result to a static type.
        val result = invokeDynamic(*values)

        return if (result is DynamicValue) {
            DynamicValueConverter(result).convertTo(T::class, parentContext)
        } else {
            result
        } as? V
            ?: throw IllegalArgumentException("Unexpected lambda result: expected ${V::class}, found ${result::class}")
    }
}
