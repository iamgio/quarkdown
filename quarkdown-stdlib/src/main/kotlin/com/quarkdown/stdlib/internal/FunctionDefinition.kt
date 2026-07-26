package com.quarkdown.stdlib.internal

import com.quarkdown.core.context.MutableContext
import com.quarkdown.core.function.Function
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
 *
 * This overload accepts already-typed [FunctionParameter]s and preserves them verbatim on the
 * registered function. It is suited to callers that wrap an existing function and need to keep
 * the original parameter types (e.g. `InlineMarkdownContent`), rather than collapse everything
 * to [DynamicValue].
 *
 * @param context context to register the function in
 * @param name name the function will be callable by
 * @param parameters typed function parameters, used as-is on the registered function
 * @param invoke executed when the function is called. Receives the originating [FunctionCall],
 *               a list of parameter-aligned argument values (with [NoneValue] for unbound optional ones),
 *               and the raw [ArgumentBindings] map
 */
internal fun declareFunction(
    context: MutableContext,
    name: String,
    parameters: List<FunctionParameter<*>>,
    invoke: (call: FunctionCall<*>, args: List<Value<*>>, bindings: ArgumentBindings) -> OutputValue<*>,
) {
    val function =
        SimpleFunction(name, parameters) { bindings, call ->
            // Retrieving arguments from the function call.
            // `None` is used as a default value if the argument for an optional parameter is not provided.
            val args: List<Value<*>> = parameters.map { bindings[it]?.value ?: NoneValue }

            invoke(call, args, bindings)
        }
    declareFunction(context, function)
}

/**
 * Registers a pre-constructed [function] in [context], under the same custom-function library
 * naming convention used by the other overloads.
 *
 * Useful when the caller needs the callable to be a specific [Function] subtype rather than the
 * standard [SimpleFunction] built by the other overloads (e.g. an extension wrapper that carries
 * its own mutable state).
 */
internal fun declareFunction(
    context: MutableContext,
    function: Function<*>,
) {
    context.loadLibrary(Library(CUSTOM_FUNCTION_LIBRARY_NAME_PREFIX + function.name, setOf(function)))
}

/**
 * Registers a custom user-defined function in [context], backing both [function] and [variable].
 *
 * This overload accepts [LambdaParameter]s: each becomes a [DynamicValue]-typed [FunctionParameter],
 * which is appropriate for user-authored `.function`/`.variable` bodies where arguments arrive
 * as raw Quarkdown text and are evaluated by the user's lambda.
 *
 * @param context context to register the function in
 * @param name name the function will be callable by
 * @param bodyParameters parameter definitions, typically derived from the body lambda's explicit parameters
 * @param invoke see the typed overload
 */
internal fun declareFunctionFromLambda(
    context: MutableContext,
    name: String,
    bodyParameters: List<LambdaParameter>,
    invoke: (call: FunctionCall<*>, args: List<Value<*>>, bindings: ArgumentBindings) -> OutputValue<*>,
) {
    val parameters =
        bodyParameters.mapIndexed { index, parameter ->
            FunctionParameter(
                name = parameter.name,
                type = DynamicValue::class,
                index = index,
                isOptional = parameter.isOptional,
                isExplicitlyBody = parameter.isExplicitlyBody,
            )
        }
    declareFunction(context, name, parameters, invoke)
}
