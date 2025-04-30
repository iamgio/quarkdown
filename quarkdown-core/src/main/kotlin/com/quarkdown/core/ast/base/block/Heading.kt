package com.quarkdown.core.ast.base.block

import com.quarkdown.core.ast.InlineContent
import com.quarkdown.core.ast.attributes.LocationTrackableNode
import com.quarkdown.core.ast.attributes.id.Identifiable
import com.quarkdown.core.ast.attributes.id.IdentifierProvider
import com.quarkdown.core.ast.base.TextNode
import com.quarkdown.core.ast.base.block.Heading.Companion.marker
import com.quarkdown.core.visitor.node.NodeVisitor

/**
 * A heading defined via prefix symbols.
 * A heading is identifiable, as it can be looked up in the document and can be referenced.
 * It is also location trackable, meaning its position in the document hierarchy is determined, and possibly displayed.
 * @param depth importance (`depth=1` for H1, `depth=6` for H6)
 * @param isDecorative whether this heading is decorative.
 *                     A decorative heading does not trigger automatic page breaks and is not counted in the document's hierarchy
 *                     and is not numbered.
 * @param customId optional custom ID. If `null`, the ID is automatically generated
 */
class Heading(
    val depth: Int,
    override val text: InlineContent,
    val isDecorative: Boolean = false,
    val customId: String? = null,
) : TextNode,
    Identifiable,
    LocationTrackableNode {
    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(this)

    override fun <T> accept(visitor: IdentifierProvider<T>) = visitor.visit(this)

    /**
     * Decorative headings are not assigned a location and are not counted.
     */
    override val canTrackLocation: Boolean
        get() = !isDecorative

    /**
     * @return whether this heading is a marker
     * @see marker
     */
    val isMarker: Boolean
        get() = depth == MARKER_HEADING_DEPTH

    companion object {
        /**
         * When a [Heading] has this depth value, it is considered an invisible referenceable mark.
         * Depth 0 cannot be achieved with plain Markdown, but it can be supplied by a Quarkdown function.
         */
        private const val MARKER_HEADING_DEPTH = 0

        /**
         * Creates an invisible [Heading] that acts as a marker that can be referenced by other elements in the document.
         * A useful use case would be, for example, in combination with a [com.quarkdown.core.context.toc.TableOfContents].
         * Depth 0 cannot be achieved with plain Markdown, but it can be supplied by the Quarkdown function `.marker`.
         */
        fun marker(name: InlineContent) = Heading(MARKER_HEADING_DEPTH, name)
    }
}
