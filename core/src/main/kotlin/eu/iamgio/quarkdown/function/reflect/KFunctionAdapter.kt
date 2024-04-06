package eu.iamgio.quarkdown.function.reflect

import eu.iamgio.quarkdown.function.Function
import eu.iamgio.quarkdown.function.FunctionArgumentsLinker
import eu.iamgio.quarkdown.function.FunctionParameter
import eu.iamgio.quarkdown.function.value.DynamicInputValue
import eu.iamgio.quarkdown.function.value.InputValue
import eu.iamgio.quarkdown.function.value.OutputValue
import kotlin.reflect.KClass
import kotlin.reflect.KFunction

/**
 * A Quarkdown [Function] adapted from a regular Kotlin [KFunction].
 * @param function Kotlin function to adapt
 */
class KFunctionAdapter<T : OutputValue<*>>(private val function: KFunction<T>) : Function<T> {
    override val name: String
        get() = function.name

    @Suppress("UNCHECKED_CAST")
    override val parameters: List<FunctionParameter<*>>
        get() =
            function.parameters.map {
                FunctionParameter(
                    // TODO handle error if null
                    it.name!!,
                    // TODO handle cast errors
                    it.type.classifier as KClass<out InputValue<T>>,
                )
            }

    override val invoke: FunctionArgumentsLinker.() -> T
        get() = {
            // TODO handle mismatching types
            val args =
                this.links.map { (parameter, argument) ->
                    // The type of dynamic arguments is determined.
                    when (argument.value) {
                        is DynamicInputValue -> argument.value.convertTo(parameter.type)
                        else -> argument.value
                    }.unwrappedValue
                }

            function.call(*args.toTypedArray())
        }
}
