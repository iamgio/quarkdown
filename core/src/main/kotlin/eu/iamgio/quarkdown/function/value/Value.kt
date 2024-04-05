package eu.iamgio.quarkdown.function.value

import eu.iamgio.quarkdown.ast.Node

sealed interface Value<T> {
    val value: T
}

sealed interface InputValue<T> : Value<T>

sealed interface OutputValue<T> : Value<T>

data class StringValue(override val value: String) : InputValue<String>, OutputValue<String>

data class NumberValue(override val value: Number) : InputValue<Number>

data class NodeValue(override val value: Node) : OutputValue<Node>

class VoidValue : OutputValue<Unit> {
    override val value = Unit
}
