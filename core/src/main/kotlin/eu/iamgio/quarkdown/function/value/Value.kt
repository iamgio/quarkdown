package eu.iamgio.quarkdown.function.value

import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.function.expression.Expression
import eu.iamgio.quarkdown.function.value.output.OutputValueVisitor

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
 * When used as an [Expression], its evaluated value is the same as its static wrapped value
 */
sealed interface InputValue<T> : Value<T>, Expression {
    override fun eval(): InputValue<*> = this
}

/**
 * An immutable value wrapper that is used in function outputs.
 */
sealed interface OutputValue<T> : Value<T> {
    fun <O> accept(visitor: OutputValueVisitor<O>): O
}

/**
 * An immutable string [Value].
 */
data class StringValue(override val unwrappedValue: String) : InputValue<String>, OutputValue<String> {
    override fun <O> accept(visitor: OutputValueVisitor<O>): O = visitor.visit(this)

    override fun append(other: Expression): Expression = StringValue(this.unwrappedValue + other.eval().toString())
}

/**
 * An immutable numeric [Value].
 */
data class NumberValue(override val unwrappedValue: Number) : InputValue<Number>, OutputValue<Number> {
    override fun <O> accept(visitor: OutputValueVisitor<O>): O = visitor.visit(this)

    override fun append(other: Expression): Expression = TODO("Not yet implemented")
}

/**
 * An immutable [Node] [Value].
 */
data class NodeValue(override val unwrappedValue: Node) : OutputValue<Node> {
    override fun <O> accept(visitor: OutputValueVisitor<O>): O = visitor.visit(this)
}

/**
 * An empty [Value] with no content.
 */
class VoidValue : OutputValue<Unit> {
    override val unwrappedValue = Unit

    override fun <O> accept(visitor: OutputValueVisitor<O>): O = visitor.visit(this)
}
