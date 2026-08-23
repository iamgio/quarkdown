package com.quarkdown.core.function.call.binding

import com.quarkdown.core.function.FunctionParameter
import com.quarkdown.core.function.call.FunctionCall
import com.quarkdown.core.function.error.InvalidArgumentCountException

/**
 * Builder of bindings for all arguments of a function call.
 *
 * @param call function call to bind arguments for
 * @see RegularArgumentsBinder
 */
class AllArgumentsBinder(
    private val call: FunctionCall<*>,
) : ArgumentsBinder {
    override fun createBindings(parameters: List<FunctionParameter>): ArgumentBindings {
        val bindable = parameters.filterNot { it.isInjected }

        val bindings = RegularArgumentsBinder(call).createBindings(bindable)

        // If mandatory params count > args count.
        if (bindable.any { !it.isOptional && it !in bindings }) {
            throw InvalidArgumentCountException(call)
        }

        return bindings
    }
}
