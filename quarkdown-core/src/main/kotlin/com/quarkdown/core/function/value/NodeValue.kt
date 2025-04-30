package com.quarkdown.core.function.value

import com.quarkdown.core.ast.Node
import com.quarkdown.core.function.expression.Expression
import com.quarkdown.core.function.expression.visitor.ExpressionVisitor
import com.quarkdown.core.function.value.output.OutputValueVisitor

/**
 * An immutable [Node] [Value].
 */
data class NodeValue(
    override val unwrappedValue: Node,
) : OutputValue<Node>,
    Expression {
    override fun <O> accept(visitor: OutputValueVisitor<O>): O = visitor.visit(this)

    override fun <T> accept(visitor: ExpressionVisitor<T>): T = visitor.visit(this)
}

/**
 * @return [this] node wrapped into a [NodeValue]
 */
fun Node.wrappedAsValue() = NodeValue(this)
