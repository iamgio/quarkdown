package eu.iamgio.quarkdown.function.call

import eu.iamgio.quarkdown.function.FunctionParameter
import eu.iamgio.quarkdown.function.call.injection.Injected
import eu.iamgio.quarkdown.function.error.InvalidArgumentCountException
import eu.iamgio.quarkdown.function.error.MismatchingArgumentTypeException
import eu.iamgio.quarkdown.function.value.DynamicInputValue
import kotlin.reflect.full.isSubclassOf

/**
 * Parameter-argument pairs for a function call.
 */
private typealias Links = Map<FunctionParameter<*>, FunctionCallArgument>

/**
 * Helper that associates [FunctionCallArgument]s to their corresponding [FunctionParameter].
 * @param call function call to link arguments for
 */
class FunctionArgumentsLinker(private val call: FunctionCall<*>) {
    lateinit var links: Links

    /**
     * @return the parameter-argument pairs for regular user-supplied arguments
     */
    private fun generateRegularLinks(): Links =
        buildMap {
            val regularParameters = call.function.parameters.filterNot { it.isInjected }

            call.arguments.forEachIndexed { index, argument ->
                // Corresponding parameter.
                // Error if args count > params count.
                val parameter = regularParameters.getOrNull(index) ?: throw InvalidArgumentCountException(call)

                // The type of dynamic arguments is determined.
                val staticArgument =
                    when (val value = argument.value) {
                        // The value is dynamic and must be converted to a static type.
                        is DynamicInputValue -> {
                            // The dynamic value is converted into the expected parameter type.
                            // Throws error if the conversion could not happen.
                            val staticValue =
                                value.convertTo(parameter.type)
                                    ?: throw MismatchingArgumentTypeException(call, parameter, argument)

                            FunctionCallArgument(staticValue)
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

    /**
     * @return the parameter-argument pairs for automatically injected values
     * @see Injected
     * @see FunctionParameter.isInjected
     */
    private fun generateInjectedLinks(): Links =
        buildMap {
            val injectedParameters = call.function.parameters.filter { it.isInjected }
            injectedParameters.forEach { parameter ->
                val value = Injected.valueFromType(parameter.type, call)
                this[parameter] = FunctionCallArgument(value)
            }
        }

    /**
     * Stores the associations between [FunctionCallArgument]s and [FunctionParameter]s.
     * @throws eu.iamgio.quarkdown.function.error.InvalidFunctionCallException or subclass
     *         if there is a mismatch between arguments and parameters
     */
    fun link() {
        this.links = generateRegularLinks() + generateInjectedLinks()

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
