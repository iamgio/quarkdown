package com.quarkdown.rendering.html

import com.quarkdown.core.ast.attributes.id.IdentifierProvider
import com.quarkdown.core.ast.base.block.FootnoteDefinition
import com.quarkdown.core.ast.base.block.Heading
import com.quarkdown.core.rendering.NodeRenderer
import com.quarkdown.core.util.node.toPlainText

/**
 * Provides identifiers for elements suitable for HTML rendering.
 * @param renderer renderer that this provider should use to convert nodes to plain text via [toPlainText]
 * @see IdentifierProvider
 */
class HtmlIdentifierProvider private constructor(
    private val renderer: NodeRenderer?,
) : IdentifierProvider<String> {
    /**
     * Converts [this] string to a URI-like string removing special characters and replacing whitespaces with dashes.
     * Example: "Hello, World!" -> "hello-world"
     * @return URI-like string
     */
    private fun String.toURIString() =
        this
            .lowercase()
            .replace("\\s+".toRegex(), "-")
            .replace("[^\\p{L}\\p{N}-]".toRegex(), "")

    override fun visit(heading: Heading) =
        (heading.customId ?: heading.text.toPlainText(renderer).toURIString())
            .let(::sanitizeId)

    override fun visit(footnote: FootnoteDefinition): String =
        "__footnote-${footnote.label.toURIString()}"
            .let(::sanitizeId)

    companion object {
        /**
         * Creates an instance of [HtmlIdentifierProvider].
         * @param renderer renderer that this provider should use to convert nodes to plain text via [toPlainText]
         */
        fun of(renderer: NodeRenderer?) = HtmlIdentifierProvider(renderer)

        /**
         * Sanitizes a string for use as an HTML element `id` attribute:
         * - Strips characters that are problematic in CSS selectors and URL fragments
         *   (spaces, quotes, angle brackets, etc.).
         * - Ensures the result is not empty.
         * - Ensures the result does not start with a digit (#86).
         * @return a safe identifier string, possibly different from the original
         */
        fun sanitizeId(id: String): String {
            val stripped = id.replace("[\\s\"'<>&]".toRegex(), "")
            return when {
                stripped.isEmpty() -> "_"
                stripped.first().isDigit() -> "_$stripped"
                else -> stripped
            }
        }
    }
}
