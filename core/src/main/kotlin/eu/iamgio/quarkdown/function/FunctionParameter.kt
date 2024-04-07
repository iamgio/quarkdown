package eu.iamgio.quarkdown.function

import eu.iamgio.quarkdown.function.value.InputValue
import kotlin.reflect.KClass

/**
 * A declared [Function] parameter.
 * @param name name of the parameter
 * @param type expected input value type
 * @param index index of the parameter in the function signature
 * @param isOptional whether the corresponding argument can be omitted
 * @param T input type of the parameter
 */
data class FunctionParameter<T>(
    val name: String,
    val type: KClass<out InputValue<T>>,
    val index: Int,
    val isOptional: Boolean = false,
)
