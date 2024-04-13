package eu.iamgio.quarkdown.function.call.injection

import eu.iamgio.quarkdown.context.Context
import eu.iamgio.quarkdown.function.call.FunctionCall
import eu.iamgio.quarkdown.function.value.ObjectValue
import kotlin.reflect.KClass

/**
 * When a library function parameter is annotated with `@Injected`, its value is not supplied by a function call
 * but rather automatically injected by [eu.iamgio.quarkdown.function.call.FunctionArgumentsLinker].
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class Injected {
    companion object {
        /**
         * @param type type of the target parameter to inject value to
         * @param call function call to extract injectable data from
         * @return the function-call-ready value to inject
         * @throws IllegalArgumentException if the target type is not injectable
         */
        fun valueFromType(
            type: KClass<*>,
            call: FunctionCall<*>,
        ): ObjectValue<*> =
            when (type) {
                Context::class -> ObjectValue(call.context)
                else -> throw IllegalArgumentException("Cannot inject a value to type $type")
            }
    }
}
