package eu.iamgio.quarkdown.function

import eu.iamgio.quarkdown.function.value.InputValue
import kotlin.reflect.KClass

/**
 * A declared [Function] parameter.
 * @param name name of the parameter
 * @param type expected input value type
 * @param T input type of the parameter
 */
data class FunctionParameter<T>(
    val name: String,
    val type: KClass<InputValue<T>>,
)
