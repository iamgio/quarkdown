package eu.iamgio.quarkdown.function.value

import eu.iamgio.quarkdown.ast.MarkdownContent
import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.function.expression.Expression
import eu.iamgio.quarkdown.function.expression.visitor.ExpressionVisitor
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
sealed interface InputValue<T> : Value<T>, Expression

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
    override fun <T> accept(visitor: ExpressionVisitor<T>): T = visitor.visit(this)

    override fun <O> accept(visitor: OutputValueVisitor<O>): O = visitor.visit(this)
}

/**
 * @return [this] string wrapped into a [StringValue]
 */
fun String.wrappedAsValue() = StringValue(this)

/**
 * An immutable numeric [Value].
 */
data class NumberValue(override val unwrappedValue: Number) : InputValue<Number>, OutputValue<Number> {
    override fun <T> accept(visitor: ExpressionVisitor<T>): T = visitor.visit(this)

    override fun <O> accept(visitor: OutputValueVisitor<O>): O = visitor.visit(this)
}

/**
 * @return [this] number wrapped into a [NumberValue]
 */
fun Number.wrappedAsValue() = NumberValue(this)

/**
 * An immutable boolean [Value].
 */
data class BooleanValue(override val unwrappedValue: Boolean) : InputValue<Boolean>, OutputValue<Boolean> {
    override fun <T> accept(visitor: ExpressionVisitor<T>): T = visitor.visit(this)

    override fun <O> accept(visitor: OutputValueVisitor<O>): O = visitor.visit(this)
}

/**
 * @return [this] boolean wrapped into a [BooleanValue]
 */
fun Boolean.wrappedAsValue() = BooleanValue(this)

/**
 * A [Value] that wraps an element from a static enum class.
 */
data class EnumValue(override val unwrappedValue: Enum<*>) : InputValue<Enum<*>> {
    override fun <T> accept(visitor: ExpressionVisitor<T>): T = visitor.visit(this)
}

/**
 * @return [this] enum wrapped into an [EnumValue]
 */
fun Enum<*>.wrappedAsValue() = EnumValue(this)

/**
 * A [Value] that wraps an element from a static enum class.
 */
data class ObjectValue<T>(override val unwrappedValue: T) : InputValue<T> {
    override fun <T> accept(visitor: ExpressionVisitor<T>): T = visitor.visit(this)
}

/**
 * A sub-AST that contains Markdown nodes. This is usually accepted in 'body' parameters.
 */
data class MarkdownContentValue(override val unwrappedValue: MarkdownContent) : InputValue<MarkdownContent> {
    override fun <T> accept(visitor: ExpressionVisitor<T>): T = visitor.visit(this)

    /**
     * @return this content as a [NodeValue], suitable for function outputs
     */
    fun asNodeValue(): NodeValue = NodeValue(unwrappedValue)
}

/**
 * @return [this] Markdown content wrapped into a [MarkdownContentValue]
 */
fun MarkdownContent.wrappedAsValue() = MarkdownContentValue(this)

/**
 * An immutable [Node] [Value].
 */
data class NodeValue(override val unwrappedValue: Node) : OutputValue<Node> {
    override fun <O> accept(visitor: OutputValueVisitor<O>): O = visitor.visit(this)
}

/**
 * @return [this] node wrapped into a [NodeValue]
 */
fun Node.wrappedAsValue() = NodeValue(this)

/**
 * An empty [Value] with no content.
 */
object VoidValue : OutputValue<Unit> {
    override val unwrappedValue = Unit

    override fun <O> accept(visitor: OutputValueVisitor<O>): O = visitor.visit(this)
}
