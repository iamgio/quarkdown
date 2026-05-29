package com.quarkdown.stdlib.internal

import com.quarkdown.core.context.MutableContext
import com.quarkdown.core.function.FunctionParameter
import com.quarkdown.core.function.SimpleFunction
import com.quarkdown.core.function.call.FunctionCall
import com.quarkdown.core.function.call.binding.ArgumentBindings
import com.quarkdown.core.function.library.Library
import com.quarkdown.core.function.value.DynamicValue
import com.quarkdown.core.function.value.NoneValue
import com.quarkdown.core.function.value.OutputValue
import com.quarkdown.core.function.value.Value
import com.quarkdown.core.function.value.data.LambdaParameter
import com.quarkdown.stdlib.function
import com.quarkdown.stdlib.variable

/**
 * Custom functions (via [function]) and variables (via [variable]) are saved in a [Library]
 * whose name begins by this string.
 */
internal const val CUSTOM_FUNCTION_LIBRARY_NAME_PREFIX = "__func__"

/**
 * Registers a custom user-defined function in [context], backing both [function] and [variable].
 * @param context context to register the function in
 * @param name name the function will be callable by
 * @param bodyParameters parameter definitions, typically derived from the body lambda's explicit parameters
 * @param invoke executed when the function is called. Receives the originating [FunctionCall],
 *               a list of parameter-aligned argument values (with [NoneValue] for unbound optional ones),
 *               and the raw [ArgumentBindings] map
 */
internal fun declareFunction(
    context: MutableContext,
    name: String,
    bodyParameters: List<LambdaParameter>,
    invoke: (call: FunctionCall<*>, args: List<Value<*>>, bindings: ArgumentBindings) -> OutputValue<*>,
) {
    // Function parameters.
    val parameters =
        bodyParameters.mapIndexed { index, parameter ->
            FunctionParameter(parameter.name, type = DynamicValue::class, index, parameter.isOptional)
        }

    // The custom function itself.
    val function =
        SimpleFunction(name, parameters) { bindings, call ->
            // Retrieving arguments from the function call.
            // `None` is used as a default value if the argument for an optional parameter is not provided.
            val args: List<Value<*>> = parameters.map { bindings[it]?.value ?: NoneValue }

            invoke(call, args, bindings)
        }

    // The function is registered and ready to be called.
    val library = Library(CUSTOM_FUNCTION_LIBRARY_NAME_PREFIX + name, setOf(function))
    context.loadLibrary(library)
}
