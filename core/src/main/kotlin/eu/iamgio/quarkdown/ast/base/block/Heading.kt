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

    /**
     * @return whether this heading is a marker
     * @see MARKER_HEADING_DEPTH
     */
    val isMarker: Boolean
        get() = depth == MARKER_HEADING_DEPTH

    companion object {
        /**
         * When a [Heading] has this depth value, it is considered an invisible referenceable mark,
         * that can be useful, for example, when using a [eu.iamgio.quarkdown.ast.quarkdown.block.TableOfContents].
         * Depth 0 cannot be achieved with plain Markdown, but it can be supplied by a Quarkdown function.
         */
        const val MARKER_HEADING_DEPTH = 0
    }
}
