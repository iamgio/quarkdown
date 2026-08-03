package com.quarkdown.rendering.html.post.resources

import com.quarkdown.core.context.Context
import com.quarkdown.core.document.sub.Subdocument
import java.io.StringWriter
import javax.xml.stream.XMLOutputFactory
import javax.xml.stream.XMLStreamWriter

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

    override fun buildResourceContent(subdocuments: Sequence<Pair<Subdocument, Context>>): String {
        val writer = StringWriter()
        val xml = XMLOutputFactory.newInstance().createXMLStreamWriter(writer)

        xml.writeStartDocument("UTF-8", "1.0")
        xml.writeStartElement("urlset")
        xml.writeDefaultNamespace(SITEMAP_NAMESPACE)

        // Root document.
        writeUrl(xml, baseUrl)

        // Subdocuments.
        subdocuments.forEach { (subdocument, subdocumentContext) ->
            val url = super.getSubdocumentUrl(subdocument, subdocumentContext, extension = null)
            writeUrl(xml, url)
        }

        xml.writeEndElement()
        xml.writeEndDocument()
        xml.flush()

        return writer.toString()
    }

    private fun writeUrl(
        xml: XMLStreamWriter,
        absoluteUrl: String,
    ) {
        xml.writeStartElement("url")
        xml.writeStartElement("loc")
        xml.writeCharacters(absoluteUrl)
        xml.writeEndElement()
        xml.writeEndElement()
    }
}
