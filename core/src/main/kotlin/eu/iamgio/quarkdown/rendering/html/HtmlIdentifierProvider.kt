package eu.iamgio.quarkdown.rendering.html

import eu.iamgio.quarkdown.ast.Heading
import eu.iamgio.quarkdown.ast.id.IdentifierProvider
import eu.iamgio.quarkdown.rendering.NodeRenderer
import eu.iamgio.quarkdown.util.toPlainText
import java.net.URLEncoder

/**
 * Provides identifiers for elements suitable for HTML rendering.
 * @param renderer renderer that uses this provider
 * @see eu.iamgio.quarkdown.ast.id.IdentifierProvider
 */
class HtmlIdentifierProvider private constructor(private val renderer: NodeRenderer) : IdentifierProvider<String> {
    /**
     * Converts [this] string to a URI-like string.
     * Example: "Hello, World!" -> "hello-world%21"
     * @return URI-like string
     */
    private fun String.toURIString() =
        this.lowercase()
            .replace(" ", "-")
            .let { URLEncoder.encode(it, Charsets.UTF_8.name())!! }

    override fun visit(heading: Heading) = heading.text.toPlainText(renderer).toURIString()

    companion object {
        /**
         * Creates an instance of [HtmlIdentifierProvider].
         * @param renderer renderer that uses this provider
         */
        fun of(renderer: NodeRenderer) = HtmlIdentifierProvider(renderer)
    }
}
