package com.quarkdown.core.ast.base.inline

import com.quarkdown.core.ast.Node
import com.quarkdown.core.visitor.node.NodeVisitor

/**
 * A soft line break within a paragraph, representing a newline in the source
 * that is not a hard line break (i.e., not preceded by two spaces or a backslash).
 *
 * The rendering of a soft break depends on the document language:
 * for CJK languages (Chinese, Japanese, Korean) no space is inserted,
 * while for other languages a space is inserted.
 */
object SoftBreak : Node {
    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(this)
}
