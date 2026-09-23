package com.quarkdown.core.ast.base.inline

import com.quarkdown.core.misc.color.Color
import com.quarkdown.core.visitor.node.NodeVisitor

/**
 * Inline code.
 * @param text text content
 * @param content additional content this code holds, if any
 */
class CodeSpan(
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
    data class ColorContent(
        val color: Color,
    ) : ContentInfo
}
