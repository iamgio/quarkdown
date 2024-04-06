package eu.iamgio.quarkdown.function.value

import eu.iamgio.quarkdown.ast.Node

/**
 * An immutable value wrapper.
 */
sealed interface Value<T> {
    /**
     * The wrapped value.
     */
    val unwrappedValue: T
}

/**
 * An immutable value wrapper that is used in function parameters and function call arguments.
 */
sealed interface InputValue<T> : Value<T>

/**
 * An immutable value wrapper that is used in function outputs.
 */
sealed interface OutputValue<T> : Value<T>

/**
 * An immutable string [Value].
 */
data class StringValue(override val unwrappedValue: String) : InputValue<String>, OutputValue<String>

/**
 * An immutable numeric [Value].
 */
data class NumberValue(override val unwrappedValue: Number) : InputValue<Number>

/**
 * An immutable [Node] [Value].
 */
data class NodeValue(override val unwrappedValue: Node) : OutputValue<Node>

/**
 * An empty [Value] with no content.
 */
class VoidValue : OutputValue<Unit> {
    override val unwrappedValue = Unit
}
