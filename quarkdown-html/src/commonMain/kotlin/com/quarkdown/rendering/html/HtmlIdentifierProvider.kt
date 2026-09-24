package com.quarkdown.rendering.html

import com.quarkdown.core.ast.attributes.id.IdentifierProvider
import com.quarkdown.core.ast.attributes.id.getIdentifierDeduplicationIndex
import com.quarkdown.core.ast.base.block.FootnoteDefinition
import com.quarkdown.core.ast.base.block.Heading
import com.quarkdown.core.context.Context
import com.quarkdown.core.rendering.NodeRenderer
import com.quarkdown.core.util.node.toPlainText
import com.quarkdown.core.util.sanitizeAsIdentifier
import com.quarkdown.core.util.toUriIdentifier

/**
 * Provides identifiers for elements suitable for HTML rendering.
 * @param renderer renderer that this provider should use to convert nodes to plain text via [toPlainText]
 * @param context optional context used to disambiguate colliding identifiers via
 *                [getIdentifierDeduplicationIndex]. When omitted, headings keep their base identifier
 *                even if it collides with other headings in the document.
 * @see IdentifierProvider
 */
class HtmlIdentifierProvider private constructor(
    private val renderer: NodeRenderer?,
    private val context: Context?,
) : IdentifierProvider<String> {
    override fun visit(heading: Heading): String {
        val baseId = heading.customId ?: heading.text.toPlainText(renderer).toUriIdentifier()
        // Disambiguating colliding identifiers.
        val id =
            when (val index = context?.let(heading::getIdentifierDeduplicationIndex) ?: 0) {
                0 -> baseId
                else -> "$baseId-${index + 1}"
            }
        return sanitizeId(id)
    }

    override fun visit(footnote: FootnoteDefinition): String =
        "__footnote-${footnote.label.toUriIdentifier()}"
            .let(::sanitizeId)

    companion object {
        /**
         * Creates an instance of [HtmlIdentifierProvider].
         * @param renderer renderer that this provider should use to convert nodes to plain text via [toPlainText]
         * @param context optional context used to disambiguate colliding identifiers
         */
        fun of(
            renderer: NodeRenderer?,
            context: Context? = null,
        ) = HtmlIdentifierProvider(renderer, context)

        /**
         * Sanitizes a string for use as an HTML element `id` attribute.
         */
        fun sanitizeId(id: String): String = id.sanitizeAsIdentifier()
    }
}
