package eu.iamgio.quarkdown.function

import eu.iamgio.quarkdown.function.error.InvalidArgumentCountException
import eu.iamgio.quarkdown.function.error.MismatchingArgumentTypeException
import eu.iamgio.quarkdown.function.value.DynamicInputValue
import kotlin.reflect.full.isSubclassOf

/**
 * Helper that associates [FunctionCallArgument]s to their corresponding [FunctionParameter].
 * @param call function call to link arguments for
 */
class FunctionArgumentsLinker(private val call: FunctionCall<*>) {
    lateinit var links: Map<FunctionParameter<*>, FunctionCallArgument>

    /**
     * Stores the associations between [FunctionCallArgument]s and [FunctionParameter]s.
     * @throws eu.iamgio.quarkdown.function.error.InvalidFunctionCallException or subclass
     *         if there is a mismatch between arguments and parameters
     */
    fun link() {
        this.links =
            buildMap {
                for ((index, argument) in call.arguments.withIndex()) {
                    // If args count > params count.
                    val parameter =
                        call.function.parameters.getOrNull(index)
                            ?: throw InvalidArgumentCountException(call)

                    // The type of dynamic arguments is determined.
                    val staticArgument =
                        when (argument.expression) {
                            is DynamicInputValue -> {
                                // Throw error if the conversion could not happen.
                                val value =
                                    argument.expression.convertTo(parameter.type)
                                        ?: throw MismatchingArgumentTypeException(call, parameter, argument)

                                FunctionCallArgument(value)
                            }

                            else -> argument
                        }

                    // Type match check.
                    if (!staticArgument.value.unwrappedValue!!::class.isSubclassOf(parameter.type) &&
                        !staticArgument.value::class.isSubclassOf(parameter.type)
                    ) {
                        throw MismatchingArgumentTypeException(call, parameter, staticArgument)
                    }

                    // Add link.
                    this[parameter] = staticArgument
                }
            }

        call.function.parameters.forEach { parameter ->
            // If mandatory params count > args count.
            if (!parameter.isOptional && parameter !in this.links) {
                throw InvalidArgumentCountException(call)
            }
        }
    }

    /**
     * @param name name of the parameter to get the corresponding argument value for
     * @param T type of the value
     * @return the value of the argument by the given name
     * @throws NoSuchElementException if [name] does not match any parameter name
     */
    inline fun <reified T> arg(name: String): T =
        this.links.entries
            .first { it.key.name == name }
            .value // Map.Entry method: returns FunctionCallArgument
            .value // FunctionCallArgument method: returns InputValue<T>
            .unwrappedValue as T // InputValue<T> method: returns T
}
