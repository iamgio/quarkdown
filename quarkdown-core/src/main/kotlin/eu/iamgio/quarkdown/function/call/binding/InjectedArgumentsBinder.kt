package eu.iamgio.quarkdown.function.call.binding

import eu.iamgio.quarkdown.function.FunctionParameter
import eu.iamgio.quarkdown.function.call.FunctionCall
import eu.iamgio.quarkdown.function.call.FunctionCallArgument
import eu.iamgio.quarkdown.function.reflect.InjectedValue

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
