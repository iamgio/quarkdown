package eu.iamgio.quarkdown.function.call.binding

import eu.iamgio.quarkdown.function.FunctionParameter
import eu.iamgio.quarkdown.function.call.FunctionCall
import eu.iamgio.quarkdown.function.error.InvalidArgumentCountException
import eu.iamgio.quarkdown.function.error.MismatchingArgumentTypeException
import eu.iamgio.quarkdown.function.error.UnnamedArgumentAfterNamedException
import eu.iamgio.quarkdown.function.error.UnresolvedParameterException
import eu.iamgio.quarkdown.function.reflect.DynamicValueConverter
import eu.iamgio.quarkdown.function.value.DynamicValue
import eu.iamgio.quarkdown.function.value.StringValue
import eu.iamgio.quarkdown.function.value.ValueFactory
import kotlin.reflect.full.isSubclassOf

/**
 * Builder of bindings for the regular (not injected) argument subset of a function call.
 * @param call function call to bind arguments for
 * @see InjectedArgumentsBinder for the injected argument subset
 */
class RegularArgumentsBinder(private val call: FunctionCall<*>) : ArgumentsBinder {
    override fun createBindings(parameters: List<FunctionParameter<*>>) =
        buildMap {
            var encounteredNamedArgument = false

            call.arguments.forEachIndexed { index, argument ->
                // Corresponding parameter.
                val parameter =
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
                        !encounteredNamedArgument -> parameters.getOrNull(index)
                        // Unnamed arguments cannot appear after a named one.
                        else -> throw UnnamedArgumentAfterNamedException(call)
                    } ?: throw InvalidArgumentCountException(call) // Error if args count > params count.

                val value = argument.value
                // The type of dynamic arguments is determined.
                val staticArgument =
                    when {
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
                                DynamicValueConverter(value).convertTo(parameter.type, call.context)
                                    ?: throw MismatchingArgumentTypeException(call, parameter, argument)

                            argument.copy(expression = staticValue)
                        }

                        // If the expected type is a string but the argument isn't,
                        // it is automatically converted to a string.
                        value !is StringValue && parameter.type == String::class -> {
                            argument.copy(expression = ValueFactory.string(value.unwrappedValue.toString()))
                        }

                        else -> argument
                    }

                // Type match check.
                if (
                    !staticArgument.value.unwrappedValue!!::class.isSubclassOf(parameter.type) &&
                    !staticArgument.value::class.isSubclassOf(parameter.type)
                ) {
                    throw MismatchingArgumentTypeException(call, parameter, staticArgument)
                }

                // Add link.
                this[parameter] = staticArgument
            }
        }
}
