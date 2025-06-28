package com.quarkdown.core.ast.base.block

import com.quarkdown.core.ast.InlineContent
import com.quarkdown.core.ast.base.TextNode
import com.quarkdown.core.visitor.node.NodeVisitor

/**
 * Creation of a referenceable footnote definition.
 * @param label inline content of the referenceable label
 * @param text inline content of the footnote
 */
class FootnoteDefinition(
    val label: String,
    override val text: InlineContent,
) : TextNode {
    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(this)
}
