package com.quarkdown.rendering.html.post.resources

import com.quarkdown.core.context.Context
import com.quarkdown.core.document.sub.Subdocument
import com.quarkdown.core.util.Escape

private const val SITEMAP_FILE_NAME = "sitemap.xml"

@Suppress("HttpUrlsUsage")
private const val SITEMAP_NAMESPACE = "http://www.sitemaps.org/schemas/sitemap/0.9"

/**
 * A [PostRendererResource] that generates a `sitemap.xml` listing the root document
 * and every subdocument as a `<url><loc>` entry with absolute URLs.
 *
 * Requires [com.quarkdown.core.context.options.HtmlOptions.baseUrl] to be set via `.htmloptions`;
 * if absent, no sitemap is emitted. The sitemap is also skipped when the document has no subdocuments.
 *
 * @param context the context of the document being rendered, used to access the base URL,
 *        subdocument graph, and output naming strategy
 */
class SitemapPostRendererResource(
    context: Context,
) : SubdocumentMapperPostRendererResource(context) {
    override val runsInPreviewMode: Boolean = false

    override val resourceName: String
        get() = SITEMAP_FILE_NAME

    override fun buildResourceContent(subdocuments: Sequence<Pair<Subdocument, Context>>): String =
        buildString {
            append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
            append("<urlset xmlns=\"").append(SITEMAP_NAMESPACE).append("\">")

            // Root document.
            appendUrl(baseUrl)

            // Subdocuments.
            subdocuments.forEach { (subdocument, subdocumentContext) ->
                val url = super.getSubdocumentUrl(subdocument, subdocumentContext, extension = null)
                appendUrl(url)
            }

            append("</urlset>")
        }

    private fun StringBuilder.appendUrl(absoluteUrl: String) {
        append("<url><loc>")
        append(Escape.Xml.escape(absoluteUrl))
        append("</loc></url>")
    }
}
