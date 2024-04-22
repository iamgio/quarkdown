package eu.iamgio.quarkdown.function.error

import eu.iamgio.quarkdown.function.call.FunctionCall
import eu.iamgio.quarkdown.function.call.FunctionCallArgument

/**
 * An exception thrown if a named argument does not match the name of any parameter from the called function.
 * @param argument the named argument
 * @param call the invalid call
 * @see FunctionCallArgument.isNamed
 */
class UnresolvedParameterException(argument: FunctionCallArgument, call: FunctionCall<*>) :
    InvalidFunctionCallException(
        call,
        reason = "cannot find parameter ${argument.name}, which was referenced by a named argument",
    )
