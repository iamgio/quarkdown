package eu.iamgio.quarkdown.ast.base.block

import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.function.value.data.Range
import eu.iamgio.quarkdown.visitor.node.NodeVisitor

/**
 * A code block.
 * @param content code content
 * @param language optional syntax language
 * @param showLineNumbers whether to show line numbers
 * @param focusedLines range of lines to focus on. No lines are focused if `null`
 */
data class Code(
    val content: String,
    val language: String?,
    val showLineNumbers: Boolean = true,
    val focusedLines: Range? = null,
) : Node {
    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(this)
}
