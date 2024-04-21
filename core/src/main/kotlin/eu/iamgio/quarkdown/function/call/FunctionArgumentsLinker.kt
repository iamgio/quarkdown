package eu.iamgio.quarkdown.function.call

import eu.iamgio.quarkdown.function.FunctionParameter
import eu.iamgio.quarkdown.function.error.InvalidArgumentCountException
import eu.iamgio.quarkdown.function.error.MismatchingArgumentTypeException
import eu.iamgio.quarkdown.function.reflect.DynamicValueConverter
import eu.iamgio.quarkdown.function.reflect.Injected
import eu.iamgio.quarkdown.function.reflect.InjectedValue
import eu.iamgio.quarkdown.function.value.DynamicValue
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
     * @param parameters regular (non-injected) parameters of the function
     * @return the parameter-argument pairs for regular user-supplied arguments
     */
    private fun generateRegularLinks(parameters: List<FunctionParameter<*>>): Links =
        buildMap {
            call.arguments.forEachIndexed { index, argument ->
                // Corresponding parameter.
                val parameter =
                    when {
                        // A body parameter is always the last one in the function signature.
                        argument.isBody -> parameters.lastOrNull()
                        // Non-body parameters follow the index.
                        else -> parameters.getOrNull(index)
                    }

                // Error if args count > params count.
                if (parameter == null) {
                    throw InvalidArgumentCountException(call)
                }

                // The type of dynamic arguments is determined.
                val staticArgument =
                    when (val value = argument.value) {
                        // The value is dynamic and must be converted to a static type.
                        is DynamicValue -> {
                            // The dynamic value is converted into the expected parameter type.
                            // Throws error if the conversion could not happen.
                            val staticValue =
                                DynamicValueConverter(value).convertTo(parameter.type, call)
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
     * @param parameters injected parameters of the function
     * @return the parameter-argument pairs for automatically injected values
     * @see Injected
     * @see FunctionParameter.isInjected
     */
    private fun generateInjectedLinks(parameters: List<FunctionParameter<*>>): Links =
        parameters.associateWith {
            val value = InjectedValue.fromType(it.type, call)
            FunctionCallArgument(value)
        }

    /**
     * Stores the associations between [FunctionCallArgument]s and [FunctionParameter]s.
     * @throws eu.iamgio.quarkdown.function.error.InvalidFunctionCallException or subclass
     *         if there is a mismatch between arguments and parameters
     */
    fun link() {
        // Injected and non-injected parameters are handled separately.
        val (injected, regular) = call.function.parameters.partition { it.isInjected }

        // Argument-parameter links are generated for both types of parameters and joined together.
        this.links = generateRegularLinks(regular) + generateInjectedLinks(injected)

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
     * @deprecated used in tests only
     */
    inline fun <reified T> arg(name: String): T =
        this.links.entries
            .first { it.key.name == name }
            .value // Map.Entry method: returns FunctionCallArgument
            .value // FunctionCallArgument method: returns InputValue<T>
            .unwrappedValue as T // InputValue<T> method: returns T
}
