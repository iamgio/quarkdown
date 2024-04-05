package eu.iamgio.quarkdown.function.value

import eu.iamgio.quarkdown.ast.Node

/**
 *
 */
sealed interface ValueType<T>

sealed interface InputValueType<T> : ValueType<T>

sealed interface OutputValueType<T> : ValueType<T>

object StringType : InputValueType<String>, OutputValueType<String>

object NumberType : InputValueType<Number>

object NodeType : OutputValueType<Node>

object VoidType : OutputValueType<Unit>
