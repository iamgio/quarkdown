package com.quarkdown.core.bibliography.style.dsl

import com.quarkdown.core.ast.InlineContent
import com.quarkdown.core.ast.dsl.InlineAstBuilder
import com.quarkdown.core.ast.dsl.buildInline
import com.quarkdown.core.bibliography.BibliographyEntry
import com.quarkdown.core.bibliography.style.BibliographyEntryContentProviderStrategy
import com.quarkdown.core.bibliography.style.formatAuthors

private const val LEFT_TYPOGRAPHIC_QUOTE = "“"
private const val RIGHT_TYPOGRAPHIC_QUOTE = "”"

/**
 * Builds content for a [BibliographyEntryContentProviderStrategy].
 *
 * Example usage:
 * ```kotlin
 * buildBibliographyContent(entry) {
 *     authors
 *     ", " then it.title
 *     ", " and it.journal.emphasized
 *     ".".just
 * }
 * ```
 * @param entry the bibliography entry to build content for
 * @param block the DSL block to define the content structure
 * @return the built [InlineContent] AST
 */
fun <E : BibliographyEntry> buildBibliographyContent(
    entry: E,
    block: BibliographyEntryContentBuilder<E>.(E) -> Unit,
): InlineContent =
    BibliographyEntryContentBuilder(entry, InlineAstBuilder())
        .apply { block(entry) }
        .build()

/**
 * DSL builder for creating content for a [BibliographyEntry].
 * @param entry the bibliography entry to build content for
 * @param ast the [InlineAstBuilder] to use for building inline content
 * @param E the type of the bibliography entry
 * @see buildBibliographyContent
 */
class BibliographyEntryContentBuilder<E : BibliographyEntry>(
    private val entry: E,
    private val ast: InlineAstBuilder,
) {
    private fun toContent(text: String): InlineContent = buildInline { text(text) }

    // Just

    private val InlineContent?.just: Unit?
        get() = this?.forEach { ast.run { +it } }

    /**
     * Terminal operation: pushes a single text node.
     */
    val String?.just: Unit?
        get() = this?.let(ast::text)

    // And

    /**
     * Combines two [InlineContent] if both are non-null.
     * @return the combined [InlineContent] if both are non-null, `null` otherwise
     */
    infix fun InlineContent?.and(other: InlineContent?): InlineContent? =
        when {
            this != null && other != null -> this + other
            else -> null
        }

    /**
     * Combines an [InlineContent] with a [String], which is converted to plain text.
     */
    infix fun InlineContent?.and(other: String?): InlineContent? = this and other?.let(::toContent)

    /**
     * Combines a [String], which is converted to plain text, with an [InlineContent].
     */
    infix fun String?.and(other: InlineContent?): InlineContent? = this?.let(::toContent) and other

    /**
     * Combines two [String]s to [InlineContent], converting them to plain text.
     */
    infix fun String?.and(other: String?): InlineContent? = this and other?.let(::toContent)

    // Then

    /**
     * Like [and], but terminal: pushes the result.
     */
    infix fun InlineContent?.then(other: String?) = (this and other)?.just

    /**
     * Like [and], but terminal: pushes the result.
     */
    infix fun String?.then(other: InlineContent?) = (this and other)?.just

    /**
     * Like [and], but terminal: pushes the result.
     */
    infix fun String?.then(other: String?) = (this and other)?.just

    // Modifiers

    /**
     * Makes text emphasized (italicized).
     */
    val String?.emphasized: InlineContent?
        get() =
            this?.let {
                buildInline {
                    emphasis { text(it) }
                }
            }

    /**
     * Creates a link with the given URL.
     */
    val String?.asLink: InlineContent?
        get() =
            this?.let {
                buildInline {
                    link(it) { text(it) }
                }
            }

    /**
     * Wraps the text in typographic double quotes.
     */
    val String?.inQuotes: InlineContent?
        get() = LEFT_TYPOGRAPHIC_QUOTE and this and RIGHT_TYPOGRAPHIC_QUOTE

    // Misc

    /**
     * Terminal operation: formats and pushes the authors of the bibliography entry.
     * @see BibliographyEntryContentProviderStrategy.formatAuthors
     */
    val BibliographyEntryContentProviderStrategy.authors: Unit?
        get() = formatAuthors(entry).just

    internal fun build(): InlineContent = ast.build()
}
