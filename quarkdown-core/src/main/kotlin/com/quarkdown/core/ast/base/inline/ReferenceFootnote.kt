package com.quarkdown.core.ast.base.inline

import com.quarkdown.core.ast.AstRoot
import com.quarkdown.core.ast.InlineContent
import com.quarkdown.core.ast.NestableNode
import com.quarkdown.core.ast.Node
import com.quarkdown.core.ast.attributes.reference.ReferenceNode
import com.quarkdown.core.ast.base.block.FootnoteDefinition
import com.quarkdown.core.visitor.node.NodeVisitor

/**
 * A reference to a [com.quarkdown.core.ast.base.block.FootnoteDefinition].
 * @param label reference label that should match that of the footnote definition
 * @param fallback supplier of the node to show instead of [label] in case the reference is invalid
 */
class ReferenceFootnote(
    val label: String,
    val fallback: () -> Node,
) : ReferenceNode<ReferenceFootnote, FootnoteDefinition> {
    override val reference: ReferenceFootnote = this

    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(this)
}

/**
 * An all-in-one [ReferenceFootnote] that includes its [FootnoteDefinition].
 * @param label the new label of the definition and reference
 * @param definition the content of the footnote definition
 */
class ReferenceDefinitionFootnote(
    val label: String,
    val definition: InlineContent,
) : NestableNode {
    override val children =
        listOf(
            ReferenceFootnote(
                label,
                fallback = { throw IllegalStateException("Reference + definition footnote should not need a fallback") },
            ),
            FootnoteDefinition(
                label,
                definition,
            ),
        )

    override fun <T> accept(visitor: NodeVisitor<T>): T = AstRoot(children).accept(visitor)
}
