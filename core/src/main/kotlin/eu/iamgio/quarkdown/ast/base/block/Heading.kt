package eu.iamgio.quarkdown.ast.base.block

import eu.iamgio.quarkdown.ast.InlineContent
import eu.iamgio.quarkdown.ast.base.TextNode
import eu.iamgio.quarkdown.ast.id.Identifiable
import eu.iamgio.quarkdown.ast.id.IdentifierProvider
import eu.iamgio.quarkdown.visitor.node.NodeVisitor

/**
 * A heading defined via prefix symbols.
 * A heading is identifiable, as it can be looked up in the document and can be referenced.
 * @param depth importance (`depth=1` for H1, `depth=6` for H6)
 * @param customId optional custom ID. If `null`, the ID is automatically generated
 */
data class Heading(
    val depth: Int,
    override val text: InlineContent,
    val customId: String? = null,
) : TextNode, Identifiable {
    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(this)

    override fun <T> accept(visitor: IdentifierProvider<T>) = visitor.visit(this)
}
