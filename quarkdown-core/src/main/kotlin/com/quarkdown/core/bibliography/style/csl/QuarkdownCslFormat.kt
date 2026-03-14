package com.quarkdown.core.bibliography.style.csl

import com.quarkdown.core.ast.InlineContent
import de.undercouch.citeproc.csl.internal.RenderContext
import de.undercouch.citeproc.csl.internal.SBibliography
import de.undercouch.citeproc.csl.internal.TokenBuffer
import de.undercouch.citeproc.csl.internal.format.BaseFormat
import de.undercouch.citeproc.csl.internal.token.TextToken
import de.undercouch.citeproc.output.Bibliography
import de.undercouch.citeproc.output.SecondFieldAlign

/**
 * A custom `citeproc-java` [BaseFormat] that produces Quarkdown AST nodes
 * instead of formatted strings.
 *
 * Since citeproc-java's API is string-based, the `doFormat*` callbacks return empty strings
 * while storing structured AST results in side-channel fields:
 * - [bibliographyEntries]: accumulated during [doFormatBibliographyEntry] calls.
 * - [lastCitationResult]: set by [doFormatCitation], consumed immediately after.
 *
 * Token-to-AST conversion is delegated to [CslTokenConverter].
 */
internal class QuarkdownCslFormat : BaseFormat() {
    private val tokenConverter =
        CslTokenConverter { text, type ->
            when (type) {
                TextToken.Type.DOI -> formatDOI(text)
                else -> formatURL(text)
            }
        }

    /**
     * Accumulated formatted bibliography entries, populated sequentially
     * during [de.undercouch.citeproc.CSL.makeBibliography] calls.
     */
    val bibliographyEntries: MutableList<FormattedBibliographyEntry> = mutableListOf()

    /**
     * The most recent citation result, set by [doFormatCitation]
     * and consumed by the caller immediately after.
     */
    var lastCitationResult: InlineContent = emptyList()
        private set

    override fun getName(): String = "quarkdown"

    override fun doFormatCitation(
        buffer: TokenBuffer,
        renderContext: RenderContext,
    ): String {
        lastCitationResult = tokenConverter.convert(buffer)
        return ""
    }

    override fun doFormatBibliographyEntry(
        buffer: TokenBuffer,
        renderContext: RenderContext,
        index: Int,
    ): String {
        bibliographyEntries += formatEntry(buffer, renderContext)
        return ""
    }

    override fun doFormatLink(
        text: String,
        uri: String,
    ): String = text // Links are handled directly in CslTokenConverter.

    override fun makeBibliography(
        entries: Array<out String>,
        bibliography: SBibliography,
    ): Bibliography = Bibliography(*entries)

    /**
     * Formats a single bibliography entry, splitting it into a label and content
     * when the CSL style uses
     * [second-field-align](https://docs.citationstyles.org/en/stable/specification.html#bibliography-specific-options).
     *
     * Styles with `second-field-align` (e.g. IEEE) split the token buffer into:
     * - **First-field tokens**: the entry label (e.g. `[1]`), extracted as plain text
     * - **Remaining tokens**: the entry content, converted to Quarkdown AST nodes
     *
     * Styles without it (e.g. APA) treat the entire buffer as content.
     */
    private fun formatEntry(
        buffer: TokenBuffer,
        renderContext: RenderContext,
    ): FormattedBibliographyEntry {
        val secondFieldAlign = renderContext.style.bibliography?.secondFieldAlign

        if (secondFieldAlign == null || secondFieldAlign == SecondFieldAlign.FALSE) {
            return FormattedBibliographyEntry(label = "", content = tokenConverter.convert(buffer))
        }

        val tokens = buffer.tokens
        val contentStart = tokens.indexOfFirst { !it.isFirstField }

        if (contentStart <= 0) {
            return FormattedBibliographyEntry(label = "", content = tokenConverter.convert(buffer))
        }

        val label = tokenConverter.extractPlainText(buffer.copy(0, contentStart))
        val content = tokenConverter.convert(buffer.copy(contentStart, tokens.size))
        return FormattedBibliographyEntry(label, content)
    }
}
