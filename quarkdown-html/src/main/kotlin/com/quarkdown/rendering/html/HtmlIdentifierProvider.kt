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
     * Converts [this] string to a URI-like string removing special characters and replacing spaces with dashes.
     * Example: "Hello, World!" -> "hello-world"
     * @return URI-like string
     */
    private fun String.toURIString() =
        this
            .lowercase()
            .replace(" ", "-")
            .replace("[^a-z0-9-]".toRegex(), "")

    override fun visit(heading: Heading) = heading.customId ?: heading.text.toPlainText(renderer).toURIString()

    companion object {
        /**
         * Creates an instance of [HtmlIdentifierProvider].
         * @param renderer renderer that uses this provider
         */
        fun of(renderer: NodeRenderer) = HtmlIdentifierProvider(renderer)
    }
}
