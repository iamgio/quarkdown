package com.quarkdown.core.function.call.binding

import com.quarkdown.core.function.FunctionParameter
import com.quarkdown.core.function.call.FunctionCall
import com.quarkdown.core.function.value.Value

/**
 * Converts an untyped function call argument to the static type a parameter expects.
 * @see convertArgument for the conversion the emitted implementations delegate to
 */
typealias ArgumentConverter = (
    raw: Any,
    parameter: FunctionParameter,
    call: FunctionCall<*>,
) -> Value<*>
