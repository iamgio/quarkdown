package com.quarkdown.rendering.html

import com.quarkdown.core.ast.attributes.id.IdentifierProvider
import com.quarkdown.core.ast.base.block.Heading
import com.quarkdown.core.rendering.NodeRenderer
import com.quarkdown.core.util.toPlainText

/**
 * Provides identifiers for elements suitable for HTML rendering.
 * @param renderer renderer that uses this provider
 * @see IdentifierProvider
 */
class HtmlIdentifierProvider private constructor(
    private val renderer: NodeRenderer,
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

    /**
     * Ensures that the string is a valid identifier for HTML elements:
     * - It is not empty.
     * - It does not start with a digit (#86).
     * @return a valid identifier string, possibly different from the original
     */
    private fun String.asValidId(): String =
        when {
            isEmpty() -> "_"
            first().isDigit() -> "_$this"
            else -> this
        }

    override fun visit(heading: Heading) =
        (heading.customId ?: heading.text.toPlainText(renderer).toURIString())
            .asValidId()

    companion object {
        /**
         * Creates an instance of [HtmlIdentifierProvider].
         * @param renderer renderer that uses this provider
         */
        fun of(renderer: NodeRenderer) = HtmlIdentifierProvider(renderer)
    }
}
