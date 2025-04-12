package eu.iamgio.quarkdown.function.call.binding

import eu.iamgio.quarkdown.function.FunctionParameter
import eu.iamgio.quarkdown.function.call.FunctionCall
import eu.iamgio.quarkdown.function.call.FunctionCallArgument
import eu.iamgio.quarkdown.function.error.InvalidArgumentCountException
import eu.iamgio.quarkdown.function.error.InvalidFunctionCallException
import eu.iamgio.quarkdown.function.error.MismatchingArgumentTypeException
import eu.iamgio.quarkdown.function.error.UnnamedArgumentAfterNamedException
import eu.iamgio.quarkdown.function.error.UnresolvedParameterException
import eu.iamgio.quarkdown.function.reflect.DynamicValueConverter
import eu.iamgio.quarkdown.function.value.AdaptableValue
import eu.iamgio.quarkdown.function.value.DynamicValue
import eu.iamgio.quarkdown.function.value.StringValue
import eu.iamgio.quarkdown.function.value.factory.ValueFactory
import eu.iamgio.quarkdown.pipeline.error.PipelineException
import kotlin.reflect.full.isSubclassOf

/**
 * Builder of bindings for the regular (not injected) argument subset of a function call.
 * @param call function call to bind arguments for
 * @see InjectedArgumentsBinder for the injected argument subset
 */
class RegularArgumentsBinder(private val call: FunctionCall<*>) : ArgumentsBinder {
    // As soon as a named argument is encountered, all following arguments must be named too.
    private var encounteredNamedArgument = false

    /**
     * Binds an argument to its corresponding parameter.
     * @param argument argument to bind
     * @param argumentIndex index of the argument in the call
     * @param parameters available parameters of the called function
     * @return the parameter bound to the given argument
     * @throws InvalidArgumentCountException if the number of arguments exceeds the number of parameters
     * @throws UnresolvedParameterException if the argument is named and refers to a non-existent parameter
     * @throws UnnamedArgumentAfterNamedException if an unnamed argument appears after a named one
     */
    private fun findParameter(
        argument: FunctionCallArgument,
        argumentIndex: Int,
        parameters: List<FunctionParameter<*>>,
    ): FunctionParameter<*> =
        when {
            // A body parameter is always the last one in the function signature.
            argument.isBody -> parameters.lastOrNull()
            // A non-body parameter that refers to a parameter by its name.
            argument.isNamed -> {
                encounteredNamedArgument = true
                parameters.find { it.name == argument.name }
                    ?: throw UnresolvedParameterException(argument, call)
            }
            // Non-body, unnamed parameters follow the index and cannot appear after a named argument has been encountered.
            !encounteredNamedArgument -> parameters.getOrNull(argumentIndex)
            // Unnamed arguments cannot appear after a named one.
            else -> throw UnnamedArgumentAfterNamedException(call)
        } ?: throw InvalidArgumentCountException(call) // Error if args count > params count.

    /**
     * Converts an argument to the expected type of its corresponding parameter.
     * If it's a dynamic value, it is converted to a static type via a [ValueFactory].
     * @param parameter parameter bound to the argument
     * @param argument argument to convert, which can be dynamic or static
     * @return a new argument, which holds [argument]'s value converted to the expected type
     * @throws MismatchingArgumentTypeException if the value cannot be converted to the expected type
     */
    private fun getStaticallyTypedArgument(
        parameter: FunctionParameter<*>,
        argument: FunctionCallArgument,
    ): FunctionCallArgument {
        // The value held by the argument.
        // If the argument is dynamic, it is converted to a static type.
        val value = argument.value

        return when {
            // If the expected type is dynamic, the argument is wrapped into a dynamic value.
            // For instance, custom functions defined from a Quarkdown function have dynamic-type parameters.
            parameter.type == DynamicValue::class -> {
                argument.copy(expression = DynamicValue(value.unwrappedValue))
            }

            // The value is dynamic and must be converted to a static type.
            value is DynamicValue -> {
                // The dynamic value is converted into the expected parameter type.
                // Throws error if the conversion could not happen.
                val staticValue =
                    try {
                        DynamicValueConverter(value).convertTo(parameter.type, call.context)
                    } catch (e: PipelineException) {
                        // In case the conversion fails, the error is wrapped so that it can refer to this function call as a source.
                        throw InvalidFunctionCallException(call, e.message)
                    }
                        // convertTo returns null if the called ValueFactory method returns null.
                        // This means the supplied value cannot be converted to the expected type.
                        ?: throw MismatchingArgumentTypeException(call, parameter, argument)

                argument.copy(expression = staticValue)
            }

            // If the expected type is a string but the argument isn't,
            // it is automatically converted to a string.
            value !is StringValue && parameter.type == String::class -> {
                argument.copy(expression = ValueFactory.string(value.unwrappedValue.toString()))
            }

            // If the argument does not directly match the parameter type, but is adaptable,
            // it is adapted (or at least attempted) to the expected type.
            value is AdaptableValue<*> && !value::class.isSubclassOf(parameter.type) -> {
                val adapted = value.adapt()
                when {
                    adapted::class.isSubclassOf(parameter.type) -> argument.copy(expression = adapted)
                    adapted.unwrappedValue!!::class.isSubclassOf(parameter.type) -> argument.copy(expression = adapted)
                    else -> argument
                }
            }

            else -> argument
        }
    }

    /**
     * Ensures the type of the argument matches the expected type of the parameter.
     * @param parameter parameter bound to the argument
     * @param argument statically typed argument to check
     * @throws MismatchingArgumentTypeException if the argument type does not match the parameter type
     */
    private fun checkTypeMatch(
        parameter: FunctionParameter<*>,
        argument: FunctionCallArgument,
    ) {
        if (argument.value.unwrappedValue!!::class.isSubclassOf(parameter.type)) return
        if (argument.value::class.isSubclassOf(parameter.type)) return

        throw MismatchingArgumentTypeException(call, parameter, argument)
    }

    override fun createBindings(parameters: List<FunctionParameter<*>>) =
        call.arguments.mapIndexed { index, argument ->
            // Corresponding parameter.
            val parameter = findParameter(argument, index, parameters)

            // The type of dynamic arguments is determined.
            val staticArgument = getStaticallyTypedArgument(parameter, argument)

            // Type match check.
            checkTypeMatch(parameter, staticArgument)

            // Push binding.
            parameter to staticArgument
        }.toMap()
}
