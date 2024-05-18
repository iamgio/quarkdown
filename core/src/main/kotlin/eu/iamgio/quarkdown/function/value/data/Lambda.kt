package eu.iamgio.quarkdown.function.value.data

import eu.iamgio.quarkdown.function.value.DynamicValue
import eu.iamgio.quarkdown.function.value.Value

/**
 * An action block with a variable parameter count.
 * The return type is dynamic, hence it must be later converted to a static type.
 */
interface Lambda : Function<DynamicValue>

/**
 * A lambda with no parameters.
 */
class Lambda0(private val action: () -> DynamicValue) :
    Lambda,
    () -> DynamicValue by action

/**
 * A lambda with one parameter.
 */
class Lambda1(private val action: (Value<*>) -> DynamicValue) :
    Lambda,
    (Value<*>) -> DynamicValue by action
