package com.quarkdown.core.bibliography.style.csl

import com.quarkdown.core.ast.InlineContent
import com.quarkdown.core.ast.NestableNode
import com.quarkdown.core.ast.base.inline.Text
import com.quarkdown.core.bibliography.Bibliography
import com.quarkdown.core.bibliography.BibliographyEntry
import com.quarkdown.core.bibliography.style.BibliographyEntryLabelProviderStrategy
import com.quarkdown.core.bibliography.style.BibliographyStyle
import de.undercouch.citeproc.BibliographyFileReader
import de.undercouch.citeproc.CSL
import de.undercouch.citeproc.ItemDataProvider
import java.io.InputStream

/**
 * A [BibliographyStyle] backed by a [CSL](https://citationstyles.org) style definition,
 * powered by [citeproc-java](https://github.com/michel-kraemer/citeproc-java).
 *
 * This enables support for thousands of citation styles defined in the
 * [CSL Style Repository](https://github.com/citation-style-language/styles).
 *
 * This implementation delegates both citation label and entry content formatting to citeproc-java,
 * which processes the CSL XML style definition and produces structured output
 * that is then converted to Quarkdown AST nodes via [QuarkdownCslFormat].
 *
 * @param cslStyleName the CSL style identifier (e.g. `"apa"`, `"ieee"`, `"chicago-author-date"`)
 * @param provider the item data provider supplying bibliography data to citeproc-java
 * @see QuarkdownCslFormat
 */
class CslBibliographyStyle(
    private val cslStyleName: String,
    private val provider: ItemDataProvider,
) : BibliographyStyle {
    private val format = QuarkdownCslFormat()

    private val csl =
        CSL(provider, cslStyleName).apply {
            setOutputFormat(format)
            registerCitationItems(provider.ids)
        }

    /**
     * The [Bibliography] derived from the provider's entry IDs.
     */
    val bibliography: Bibliography by lazy {
        Bibliography(
            provider.ids.associateWith { BibliographyEntry(it) },
        )
    }

    /**
     * The formatted bibliography entries, lazily generated from the CSL processor.
     * Maps each entry ID to its [QuarkdownCslFormat.FormattedEntry] (label + content).
     *
     * The CSL processor calls [QuarkdownCslFormat.doFormatBibliographyEntry] sequentially
     * for each entry, accumulating results in [QuarkdownCslFormat.bibliographyResults].
     * The provider's item IDs (in the same order as the CSL processor iterates) are then
     * mapped to these accumulated results by position.
     */
    private val formattedBibliographyEntries: Map<String, QuarkdownCslFormat.FormattedEntry> by lazy {
        format.bibliographyResults.clear()

        csl.makeBibliography()

        provider.ids.zip(format.bibliographyResults).toMap()
    }

    override val name: String
        get() = cslStyleName

    override val labelProvider =
        object : BibliographyEntryLabelProviderStrategy {
            override fun getCitationLabel(
                entry: BibliographyEntry,
                index: Int,
            ): String {
                csl.makeCitation(entry.citationKey)
                return format.lastCitationResult
                    .joinToString("") { node ->
                        extractText(node)
                    }.ifBlank { "[?]" }
            }

            override fun getListLabel(
                entry: BibliographyEntry,
                index: Int,
            ): String = formattedBibliographyEntries[entry.citationKey]?.label ?: ""
        }

    override fun contentOf(entry: BibliographyEntry): InlineContent =
        formattedBibliographyEntries[entry.citationKey]?.content ?: emptyList()

    /**
     * Recursively extracts plain text from an AST node tree.
     */
    private fun extractText(node: com.quarkdown.core.ast.Node): String =
        when (node) {
            is Text -> node.text
            is NestableNode -> node.children.joinToString("") { extractText(it) }
            else -> ""
        }

    companion object {
        /**
         * Reads a bibliography file and creates a [CslBibliographyStyle].
         * Supports BibTeX (`.bib`), CSL JSON, YAML, EndNote, and RIS formats.
         * @param cslStyleName the CSL style identifier
         * @param input the input stream for the bibliography source
         * @param filename the filename hint for format detection (e.g. `"refs.bib"`)
         * @return a new [CslBibliographyStyle]
         */
        fun from(
            cslStyleName: String,
            input: InputStream,
            filename: String,
        ): CslBibliographyStyle {
            val provider = BibliographyFileReader().readBibliographyFile(input, filename)
            return CslBibliographyStyle(cslStyleName, provider)
        }
    }
}
