package com.quarkdown.core.function.call.binding

import com.quarkdown.core.function.FunctionParameter
import com.quarkdown.core.function.call.FunctionCallArgument
import com.quarkdown.core.function.error.InvalidFunctionCallException

/**
 * Parameter-argument pairs of a function call.
 */
typealias ArgumentBindings = Map<FunctionParameter<*>, FunctionCallArgument>

/**
 * Builder of parameter-argument pairs of a function call.
 * Allows binding each argument to its corresponding parameter,
 * and may throw an exception if some cannot be paired.
 * @see InjectedArgumentsBinder
 * @see InjectedArgumentsBinder
 * @see AllArgumentsBinder
 */
sealed interface ArgumentsBinder {
    /**
     * @param parameters parameters of the called function (or a subset of them)
     * @return the parameter-argument pairs
     * @throws InvalidFunctionCallException or subclass if there is arguments and parameters cannot be paired
     */
    fun createBindings(parameters: List<FunctionParameter<*>>): ArgumentBindings
}
