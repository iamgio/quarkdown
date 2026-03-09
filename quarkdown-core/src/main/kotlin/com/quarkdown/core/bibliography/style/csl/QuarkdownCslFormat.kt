package com.quarkdown.core.bibliography.style.csl

import com.quarkdown.core.ast.InlineContent
import com.quarkdown.core.ast.Node
import com.quarkdown.core.ast.base.inline.Emphasis
import com.quarkdown.core.ast.base.inline.Link
import com.quarkdown.core.ast.base.inline.Strong
import com.quarkdown.core.ast.base.inline.Text
import com.quarkdown.core.ast.quarkdown.inline.TextTransform
import com.quarkdown.core.ast.quarkdown.inline.TextTransformData
import de.undercouch.citeproc.csl.internal.RenderContext
import de.undercouch.citeproc.csl.internal.SBibliography
import de.undercouch.citeproc.csl.internal.TokenBuffer
import de.undercouch.citeproc.csl.internal.behavior.FormattingAttributes
import de.undercouch.citeproc.csl.internal.format.BaseFormat
import de.undercouch.citeproc.csl.internal.token.DisplayGroupToken
import de.undercouch.citeproc.csl.internal.token.TextToken
import de.undercouch.citeproc.output.Bibliography
import de.undercouch.citeproc.output.SecondFieldAlign

/**
 * A custom citeproc-java [BaseFormat] that converts [TokenBuffer] tokens directly
 * into Quarkdown [InlineContent] AST nodes, instead of returning formatted strings.
 *
 * Results are accumulated in [bibliographyResults] (for bibliography entries, indexed by position)
 * and [lastCitationResult] (for the most recent citation).
 *
 * Since citeproc-java's public API is string-based, the `doFormat*` methods return empty strings
 * while storing the structured AST results in side-channel fields.
 */
internal class QuarkdownCslFormat : BaseFormat() {
    /**
     * A formatted bibliography entry, consisting of a label and content.
     * @param label the entry label (e.g. `[1]` for numbered styles, empty for author-year styles)
     * @param content the formatted entry content as Quarkdown AST nodes
     */
    data class FormattedEntry(
        val label: String,
        val content: InlineContent,
    )

    /**
     * Accumulated bibliography entry results, indexed by entry position.
     * Populated during [doFormatBibliographyEntry] calls within [Bibliography] generation.
     */
    val bibliographyResults: MutableList<FormattedEntry> = mutableListOf()

    /**
     * The most recent citation conversion result.
     * Set by [doFormatCitation] and consumed by the caller immediately after [formatCitation].
     */
    var lastCitationResult: InlineContent = emptyList()
        private set

    override fun getName(): String = "quarkdown"

    override fun doFormatCitation(
        buffer: TokenBuffer,
        renderContext: RenderContext,
    ): String {
        lastCitationResult = convertTokenBuffer(buffer)
        return ""
    }

    override fun doFormatBibliographyEntry(
        buffer: TokenBuffer,
        renderContext: RenderContext,
        index: Int,
    ): String {
        val secondFieldAlign = renderContext.style.bibliography?.secondFieldAlign

        if (secondFieldAlign != null && secondFieldAlign != SecondFieldAlign.FALSE) {
            // CSL styles with second-field-align split the entry into a label (first-field tokens)
            // and content (remaining tokens). For example, IEEE produces "[1]" as the label.
            val tokens = buffer.tokens
            val contentStart = tokens.indexOfFirst { !it.isFirstField }

            if (contentStart > 0) {
                val labelBuffer = buffer.copy(0, contentStart)
                val contentBuffer = buffer.copy(contentStart, tokens.size)
                val label = extractPlainText(labelBuffer)
                val content = convertTokenBuffer(contentBuffer)
                bibliographyResults += FormattedEntry(label, content)
                return ""
            }
        }

        bibliographyResults += FormattedEntry(label = "", content = convertTokenBuffer(buffer))
        return ""
    }

    override fun doFormatLink(
        text: String,
        uri: String,
    ): String = text // Unused: links are handled directly in convertTokenBuffer.

    override fun makeBibliography(
        entries: Array<out String>,
        bibliography: SBibliography,
    ): Bibliography = Bibliography(*entries)

    /**
     * Extracts plain text from a [TokenBuffer], concatenating all [TextToken] values.
     */
    private fun extractPlainText(buffer: TokenBuffer): String =
        buffer.tokens
            .filterIsInstance<TextToken>()
            .joinToString("") { it.text }
            .trim()

    /**
     * Converts a [TokenBuffer] into Quarkdown [InlineContent] AST nodes.
     *
     * Iterates through each token in the buffer.
     * [TextToken]s are converted to [Text], [Emphasis], [Strong], [TextTransform], or [Link] nodes
     * based on their [FormattingAttributes] and [TextToken.Type].
     * [DisplayGroupToken]s are ignored, as Quarkdown handles layout separately.
     */
    private fun convertTokenBuffer(buffer: TokenBuffer): InlineContent =
        buildList {
            for (token in buffer.tokens) {
                when (token) {
                    is TextToken -> {
                        if (token.text.isNotEmpty()) {
                            add(convertTextToken(token))
                        }
                    }
                    // Display groups are layout hints (block/indent/margin) that Quarkdown handles via CSS.
                    is DisplayGroupToken -> continue
                }
            }
        }

    /**
     * Converts a [TextToken] with its formatting attributes into a Quarkdown AST node.
     * URL/DOI tokens become [Link] nodes; other tokens are wrapped in formatting nodes
     * ([Emphasis], [Strong], [TextTransform]) based on their [FormattingAttributes].
     */
    private fun convertTextToken(token: TextToken): Node {
        val text = token.text

        // URLs and DOIs become Link nodes.
        if (token.type == TextToken.Type.URL || token.type == TextToken.Type.DOI) {
            val url =
                when (token.type) {
                    TextToken.Type.DOI -> formatDOI(text)
                    else -> formatURL(text)
                }
            return Link(
                label = listOf(Text(url)),
                url = url,
                title = null,
            )
        }

        val attrs = token.formattingAttributes
        var node: Node = Text(text)

        // Apply formatting from innermost to outermost.

        if (FormattingAttributes.getFontStyle(attrs) == FormattingAttributes.FS_ITALIC) {
            node = Emphasis(text = listOf(node))
        }

        if (FormattingAttributes.getFontWeight(attrs) == FormattingAttributes.FW_BOLD) {
            node = Strong(text = listOf(node))
        }

        if (FormattingAttributes.getFontVariant(attrs) == FormattingAttributes.FV_SMALLCAPS) {
            node =
                TextTransform(
                    data = TextTransformData(variant = TextTransformData.Variant.SMALL_CAPS),
                    children = listOf(node),
                )
        }

        return node
    }
}
