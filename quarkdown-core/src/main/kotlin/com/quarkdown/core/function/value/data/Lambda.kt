package com.quarkdown.core.function.value.data

import com.quarkdown.core.context.Context
import com.quarkdown.core.context.MutableContext
import com.quarkdown.core.function.SimpleFunction
import com.quarkdown.core.function.error.InvalidLambdaArgumentCountException
import com.quarkdown.core.function.library.Library
import com.quarkdown.core.function.reflect.DynamicValueConverter
import com.quarkdown.core.function.reflect.FromDynamicType
import com.quarkdown.core.function.value.AdaptableValue
import com.quarkdown.core.function.value.Destructurable
import com.quarkdown.core.function.value.DynamicValue
import com.quarkdown.core.function.value.NoneValue
import com.quarkdown.core.function.value.OutputValue
import com.quarkdown.core.function.value.Value
import com.quarkdown.core.function.value.factory.ValueFactory

private const val LAMBDA_LIBRARY_NAME = "__lambda-parameters__"

/**
 * A declared parameter of a [Lambda].
 * @param name name of the parameter
 * @param isOptional whether the corresponding argument can be omitted
 * @param isExplicitlyBody whether the parameter is reserved for the body argument when the lambda
 *                         is registered as a function (see [com.quarkdown.core.function.FunctionParameter.isExplicitlyBody]).
 */
data class LambdaParameter(
    val name: String,
    val isOptional: Boolean = false,
    val isExplicitlyBody: Boolean = false,
)

/**
 * An action block with a variable parameter count.
 * The return type is dynamic (a snippet of raw Quarkdown code is returned), hence it is evaluated and converted to a static type.
 * @param parentContext context this lambda lies in
 * @param explicitParameters named parameters of the lambda. If not present, parameter names are automatically set to .1, .2, etc.
 * @param action action to perform, which takes a variable sequence of [Value]s and this lambda's own forked context as arguments
 *        and returns the output of the lambda.
 */
open class Lambda(
    val parentContext: Context,
    val explicitParameters: List<LambdaParameter> = emptyList(),
    val action: (List<Value<*>>, Context) -> OutputValue<*>,
) {
    /**
     * Registers the arguments in the context, which can be accessed as function calls.
     * @param arguments arguments of the lambda action
     * @param additionalFunctions additional functions to register in the lambda execution context, alongside the lambda parameters.
     */
    private fun createLambdaParametersLibrary(
        arguments: List<Value<*>>,
        additionalFunctions: Set<SimpleFunction<*>> = emptySet(),
    ) = Library(
        LAMBDA_LIBRARY_NAME,
        functions =
            arguments
                .mapIndexed { index, argument ->
                    val parameterName = explicitParameters.getOrNull(index)?.name ?: (index + 1).toString()
                    SimpleFunction(
                        parameterName,
                        parameters = emptyList(),
                    ) { _, call ->
                        // Value associated to the lambda argument.
                        DynamicValue(argument.unwrappedValue, evaluationContext = call.context)
                    }
                }.toSet() + additionalFunctions,
    )

    /**
     * Checks if the amount of arguments matches the amount of expected parameters.
     * @param arguments arguments of the lambda action
     */
    private fun isArgumentCountValid(arguments: List<Value<*>>): Boolean {
        // If no explicit parameters are present, implicit parameters are automatically set to .1, .2, etc.,
        // hence the argument count is always valid.
        if (explicitParameters.isEmpty()) return true
        // If the amount of arguments matches the amount of mandatory parameters, the argument count is valid.
        val mandatoryParameterCount = explicitParameters.count { !it.isOptional }
        return arguments.size in mandatoryParameterCount..explicitParameters.size
    }

    /**
     * Invokes the lambda action with given arguments.
     * @param arguments arguments of the lambda action
     * @param callingContext optional context of the call site that triggered this lambda invocation.
     *                       When provided, its own libraries (e.g. lambda parameters, local variables)
     *                       are propagated to the forked execution context, allowing body arguments
     *                       containing dynamic references to resolve variables from the calling scope.
     * @param additionalFunctions additional functions to register in the lambda execution context, alongside the lambda parameters.
     * @param allowDestructuring if `true`, [arguments] has only 1 element which is [Destructurable], and the lambda has N>1 explicit parameters,
     *                           the argument is destructured into N parts.
     *                           For example, a dictionary may be destructured into its key and value.
     * @return the result of the lambda action, as an undetermined, thus dynamically-typed, value
     */
    fun invokeDynamic(
        arguments: List<Value<*>>,
        callingContext: Context? = null,
        additionalFunctions: Set<SimpleFunction<*>> = emptySet(),
        allowDestructuring: Boolean = true,
    ): OutputValue<*> {
        // Destructuring
        if (allowDestructuring && explicitParameters.size > 1) {
            // The lambda is invoked with the first N destructured components.
            (arguments.singleOrNull() as? Destructurable<*>)
                ?.let {
                    return invokeDynamic(
                        it.destructured(componentCount = explicitParameters.size),
                        callingContext,
                        additionalFunctions,
                    )
                }
        }

        // Check if the amount of arguments matches the amount of expected parameters.
        // In case parameters are not present, placeholders are automatically set to
        // .1, .2, etc., similarly to Kotlin's 'it' argument.
        // This replacement is handled by ValueFactory.lambda
        if (!isArgumentCountValid(arguments)) {
            throw InvalidLambdaArgumentCountException(explicitParameters.size, arguments.size)
        }

        // The actual arguments to pass to the lambda action, based on the given `arguments`.
        val actualArguments =
            when {
                arguments.size < explicitParameters.size -> {
                    // If the remaining parameters are optional, fill the remaining parameters with 'none' placeholder values.
                    arguments + List(explicitParameters.size - arguments.size) { NoneValue }
                }

                else -> {
                    arguments
                }
            }

        // Create a new independent context, copy of the parent one, to execute the lambda block in.
        // Upon invocation, the context is filled with the arguments information,
        // whose values can be retrieved as function calls.
        val context = parentContext.fork()

        // Propagate the calling scope's own libraries (e.g. its lambda parameters, locally defined variables)
        // so that dynamic value references passed as body arguments can resolve variables from the calling scope.
        if (callingContext is MutableContext) {
            context.libraries += callingContext.libraries
        }

        // Register the arguments in the context, which can be accessed as function calls.
        // Lambda parameters are registered last so they shadow any same-named declarations from the calling context.
        context.loadLibrary(createLambdaParametersLibrary(actualArguments, additionalFunctions))

        // The result of the lambda action is processed.
        return action(actualArguments, context)
    }

    /**
     * @see invokeDynamic
     */
    fun invokeDynamic(vararg arguments: Value<*>): OutputValue<*> = invokeDynamic(arguments.toList())

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

        return when (result) {
            is V -> result
            is DynamicValue -> DynamicValueConverter(result).convertTo(T::class, parentContext)
            is AdaptableValue<*> -> result.adapt()
            else -> result
        } as? V
            ?: throw IllegalArgumentException("Unexpected lambda result: expected ${V::class}, found ${result::class}")
    }
}
