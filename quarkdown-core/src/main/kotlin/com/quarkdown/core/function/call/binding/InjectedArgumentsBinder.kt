package com.quarkdown.core.function.call.binding

import com.quarkdown.core.function.FunctionParameter
import com.quarkdown.core.function.call.FunctionCall
import com.quarkdown.core.function.call.FunctionCallArgument
import com.quarkdown.core.function.reflect.InjectedValue

/**
 * Builder of bindings for the injected argument subset of a function call.
 * @param call function call to bind arguments for
 * @see FunctionParameter.isInjected
 */
class InjectedArgumentsBinder(private val call: FunctionCall<*>) : ArgumentsBinder {
    override fun createBindings(parameters: List<FunctionParameter<*>>) =
        parameters.associateWith {
            val value = InjectedValue.fromType(it.type, call)
            FunctionCallArgument(value)
        }
}
