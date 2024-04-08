package eu.iamgio.quarkdown.function.reflect

import eu.iamgio.quarkdown.function.Function
import eu.iamgio.quarkdown.function.FunctionParameter
import eu.iamgio.quarkdown.function.call.FunctionArgumentsLinker
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
                    name = it.name ?: "<unnamed parameter>",
                    // TODO handle cast errors
                    type = it.type.classifier as KClass<out InputValue<T>>,
                    index = it.index,
                    isOptional = it.isOptional,
                )
            }

    override val invoke: FunctionArgumentsLinker.() -> T
        get() = {
            val args =
                this.links.asSequence().associate { (parameter, argument) ->
                    // Corresponding KParameter.
                    val param = function.parameters[parameter.index]

                    param to argument.value.unwrappedValue
                }

            // Call KFunction.
            function.callBy(args)
        }
}
