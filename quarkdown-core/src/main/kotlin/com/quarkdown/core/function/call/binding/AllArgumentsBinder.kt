package com.quarkdown.core.function.call.binding

import com.quarkdown.core.function.FunctionParameter
import com.quarkdown.core.function.call.FunctionCall
import com.quarkdown.core.function.error.InvalidArgumentCountException

/**
 * Builder of bindings for all arguments of a function call.
 * @param call function call to bind arguments for
 * @see RegularArgumentsBinder
 * @see InjectedArgumentsBinder
 */
class AllArgumentsBinder(private val call: FunctionCall<*>) : ArgumentsBinder {
    /**
     * Joins the results of the subsets of regular ([RegularArgumentsBinder])
     * and injected ([InjectedArgumentsBinder]) arguments.
     */
    override fun createBindings(parameters: List<FunctionParameter<*>>): ArgumentBindings {
        val (injected, regular) = call.function.parameters.partition { it.isInjected }

        // Argument-parameter links are generated for both types of parameters and joined together.
        val bindings =
            RegularArgumentsBinder(call).createBindings(regular) +
                InjectedArgumentsBinder(call).createBindings(injected)

        // If mandatory params count > args count.
        if (call.function.parameters.any { !it.isOptional && it !in bindings }) {
            throw InvalidArgumentCountException(call)
        }

        return bindings
    }
}
