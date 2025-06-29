package com.quarkdown.core.ast.base.block

import com.quarkdown.core.ast.InlineContent
import com.quarkdown.core.ast.attributes.id.Identifiable
import com.quarkdown.core.ast.attributes.id.IdentifierProvider
import com.quarkdown.core.ast.base.TextNode
import com.quarkdown.core.visitor.node.NodeVisitor

/**
 * Creation of a footnote definition, referenceable by a [com.quarkdown.core.ast.base.inline.ReferenceFootnote].
 * @param label inline content of the referenceable label, which should match that of the [com.quarkdown.core.ast.base.inline.ReferenceFootnote]s
 * @param text inline content of the footnote
 */
class FootnoteDefinition(
    val label: String,
    override val text: InlineContent,
) : TextNode,
    Identifiable {
    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(this)

    override fun <T> accept(visitor: IdentifierProvider<T>) = visitor.visit(this)
}
