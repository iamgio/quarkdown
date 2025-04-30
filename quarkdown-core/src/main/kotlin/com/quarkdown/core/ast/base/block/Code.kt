package com.quarkdown.core.ast.base.block

import com.quarkdown.core.ast.Node
import com.quarkdown.core.function.value.data.Range
import com.quarkdown.core.visitor.node.NodeVisitor

/**
 * A code block.
 * @param content code content
 * @param language optional syntax language
 * @param showLineNumbers whether to show line numbers
 * @param focusedLines range of lines to focus on. No lines are focused if `null`
 */
class Code(
    val content: String,
    val language: String?,
    val showLineNumbers: Boolean = true,
    val focusedLines: Range? = null,
) : Node {
    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(this)
}
