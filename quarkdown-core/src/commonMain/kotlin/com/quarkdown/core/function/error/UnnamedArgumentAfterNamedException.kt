package com.quarkdown.core.function.error

import com.quarkdown.core.function.call.FunctionCall
import com.quarkdown.core.function.call.FunctionCallArgument

/**
 * An exception thrown if an unnamed argument appears after at least one named argument has been encountered in a function call.
 * @param call the invalid call
 * @see FunctionCallArgument.isNamed
 */
class UnnamedArgumentAfterNamedException(
    call: FunctionCall<*>,
) : InvalidFunctionCallException(
        call,
        reason = "all arguments following a named argument must be named as well",
    )
