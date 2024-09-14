package eu.iamgio.quarkdown.ast.base.inline

import eu.iamgio.quarkdown.misc.color.Color
import eu.iamgio.quarkdown.visitor.node.NodeVisitor

/**
 * Inline code.
 * @param text text content
 * @param content additional content this code holds, if any
 */
data class CodeSpan(
    override val text: String,
    val content: ContentInfo? = null,
) : PlainTextNode {
    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(this)

    /**
     * Additional content a [CodeSpan] may hold.
     */
    sealed interface ContentInfo

    /**
     * A color linked to a [CodeSpan].
     * For instance, this content may be assigned to a [CodeSpan] if its text holds information about a color's hex.
     * @param color color data
     */
    data class ColorContent(val color: Color) : ContentInfo
}
