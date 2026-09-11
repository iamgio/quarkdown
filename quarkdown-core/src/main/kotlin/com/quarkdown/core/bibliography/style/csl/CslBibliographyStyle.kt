package com.quarkdown.core.bibliography.style.csl

import com.quarkdown.bibliographer.Bibliographer
import com.quarkdown.bibliographer.BibliographyFormat
import com.quarkdown.bibliographer.BibliographySource
import com.quarkdown.bibliographer.FormattedEntry
import com.quarkdown.bibliographer.token.toPlainText
import com.quarkdown.core.ast.InlineContent
import com.quarkdown.core.bibliography.Bibliography
import com.quarkdown.core.bibliography.BibliographyEntry
import com.quarkdown.core.bibliography.style.BibliographyEntryLabelProviderStrategy
import com.quarkdown.core.bibliography.style.BibliographyStyle
import com.quarkdown.core.localization.Locale
import java.io.IOException
import java.io.InputStream

/**
 * A [BibliographyStyle] backed by a [CSL](https://citationstyles.org) style definition,
 * powered by [kotlin-bibliographer](https://github.com/quarkdown-labs/kotlin-bibliographer).
 *
 * This enables support for a curated selection of citation styles from the
 * [CSL Style Repository](https://github.com/citation-style-language/styles),
 * including BibTeX, CSL JSON, YAML, EndNote, and RIS bibliography sources.
 *
 * Citation label and entry content formatting are delegated to the [bibliographer],
 * whose platform-agnostic token output is converted to Quarkdown AST nodes
 * via [CslTokenConverter].
 *
 * @param cslStyleName the CSL style name (e.g. `"apa"`, `"ieee"`, `"chicago-author-date"`)
 * @param bibliographer the bibliographer rendering citations and entries
 * @see CslTokenConverter
 */
class CslBibliographyStyle(
    private val cslStyleName: String,
    private val bibliographer: Bibliographer,
) : BibliographyStyle {
    /**
     * The [Bibliography] derived from the bibliographer's entries,
     * in the order dictated by the style's sorting rules.
     */
    val bibliography: Bibliography by lazy {
        Bibliography(
            bibliographer.bibliography().associate { it.citationKey to BibliographyEntry(it.citationKey) },
        )
    }

    /**
     * Formatted bibliography entries, associated with their citation keys.
     */
    private val formattedEntries: Map<String, FormattedEntry> by lazy {
        bibliographer.bibliography().associateBy { it.citationKey }
    }

    override val name: String
        get() = cslStyleName

    override val labelProvider =
        object : BibliographyEntryLabelProviderStrategy {
            override fun getCitationLabel(entries: List<BibliographyEntry>): String =
                bibliographer
                    .citation(entries.map { it.citationKey })
                    ?.toPlainText()
                    ?: "[?]"

            override fun getListLabel(
                entry: BibliographyEntry,
                index: Int,
            ): String = formattedEntries[entry.citationKey]?.label.orEmpty()
        }

    override fun contentOf(entry: BibliographyEntry): InlineContent =
        formattedEntries[entry.citationKey]
            ?.content
            ?.let(CslTokenConverter::convert)
            ?: emptyList()

    companion object {
        /**
         * Reads a bibliography file and creates a [CslBibliographyStyle].
         * Supports BibTeX (`.bib`), CSL JSON, YAML, EndNote, and RIS formats.
         * @param cslStyleName the CSL style name, used for error reporting
         * @param cslStyleSource the serialized XML content of the CSL style definition
         * @param input the input stream for the bibliography source
         * @param filename the filename hint for format detection
         * @param locale optional [Locale] for localized terms (e.g. "and"/"und", month names).
         *               When `null`, the style's default locale is used
         * @return a new [CslBibliographyStyle]
         */
        fun from(
            cslStyleName: String,
            cslStyleSource: String,
            input: InputStream,
            filename: String,
            locale: Locale? = null,
        ): CslBibliographyStyle {
            val format =
                requireNotNull(BibliographyFormat.fromFilename(filename)) {
                    "Unsupported bibliography format for '$filename'. " +
                        "See https://quarkdown.com/wiki/bibliography for the supported formats."
                }

            val bibliographer =
                try {
                    Bibliographer(
                        style = cslStyleSource,
                        source = BibliographySource(input.reader().use { it.readText() }, format),
                        locale = locale?.tag,
                    )
                } catch (e: IOException) {
                    throw IllegalArgumentException(
                        "Bibliography style '$cslStyleName' failed to load. " +
                            "See https://quarkdown.com/wiki/bibliography for a list of available styles.",
                        e,
                    )
                }

            return CslBibliographyStyle(cslStyleName, bibliographer)
        }
    }
}
