package com.quarkdown.core.function.reflect

import com.quarkdown.core.function.Function
import com.quarkdown.core.function.FunctionParameter
import com.quarkdown.core.function.call.FunctionCall
import com.quarkdown.core.function.call.binding.ArgumentBindings
import com.quarkdown.core.function.call.validate.FunctionCallValidator
import com.quarkdown.core.function.error.FunctionCallRuntimeException
import com.quarkdown.core.function.reflect.annotation.Injected
import com.quarkdown.core.function.reflect.annotation.Name
import com.quarkdown.core.function.reflect.annotation.NoAutoArgumentUnwrapping
import com.quarkdown.core.function.reflect.annotation.NotForDocumentType
import com.quarkdown.core.function.reflect.annotation.OnlyForDocumentType
import com.quarkdown.core.function.reflect.annotation.toValidator
import com.quarkdown.core.function.value.InputValue
import com.quarkdown.core.function.value.OutputValue
import com.quarkdown.core.log.Log
import com.quarkdown.core.pipeline.error.PipelineException
import java.lang.reflect.InvocationTargetException
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation

/**
 * A Quarkdown [Function] adapted from a regular Kotlin [KFunction].
 * @param function Kotlin function to adapt
 */
class KFunctionAdapter<T : OutputValue<*>>(
    private val function: KFunction<T>,
) : Function<T> {
    /**
     * If the [Name] annotation is present on [function], the Quarkdown function name is set from there.
     * Otherwise, it is [function]'s original name.
     */
    override val name: String
        get() = function.findAnnotation<Name>()?.name ?: function.name

    @Suppress("UNCHECKED_CAST")
    override val parameters: List<FunctionParameter<*>>
        get() =
            function.parameters.map {
                FunctionParameter(
                    // If @Name is present, a custom name is set.
                    name = it.findAnnotation<Name>()?.name ?: it.name ?: "<unnamed parameter>",
                    type = it.type.classifier as KClass<out InputValue<T>>,
                    index = it.index,
                    isOptional = it.isOptional,
                    isInjected = it.hasAnnotation<Injected>(),
                )
            }

    override val validators: List<FunctionCallValidator<T>>
        get() =
            buildList {
                function.findAnnotation<OnlyForDocumentType>()?.toValidator<T>()?.let(::add)
                function.findAnnotation<NotForDocumentType>()?.toValidator<T>()?.let(::add)
            }

    override val invoke: (ArgumentBindings, FunctionCall<T>) -> T
        get() = { bindings, call ->
            val args =
                bindings.asSequence().associate { (parameter, argument) ->
                    // Corresponding KParameter.
                    val param = function.parameters[parameter.index]

                    // The argument is unwrapped unless the value class specifies not to.
                    // An example of a disabled unwrapping is DynamicValue, which is used to pass dynamically typed values as-is.
                    val arg =
                        argument.value.let {
                            if (it::class.hasAnnotation<NoAutoArgumentUnwrapping>()) it else it.unwrappedValue
                        }

                    param to arg
                }

            // Call the KFunction.
            try {
                function.callBy(args)
            } catch (e: InvocationTargetException) {
                // Exceptions thrown within the called function are converted to Quarkdown exceptions
                // and handled accordingly by the pipeline's function expander component.
                Log.debug("(expected, received): " + args.map { it.key.type to it.value })

                // If the exception comes from a nested function call, it is rethrown to go up the stack.
                throw e.targetException as? PipelineException
                    ?: FunctionCallRuntimeException(call, e.targetException)
            }
        }
}
