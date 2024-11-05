package eu.iamgio.quarkdown.stdlib

import eu.iamgio.quarkdown.ast.InlineMarkdownContent
import eu.iamgio.quarkdown.ast.MarkdownContent
import eu.iamgio.quarkdown.ast.base.block.Heading
import eu.iamgio.quarkdown.ast.quarkdown.block.toc.TableOfContentsView
import eu.iamgio.quarkdown.ast.quarkdown.inline.PageCounter
import eu.iamgio.quarkdown.ast.quarkdown.invisible.PageMarginContentInitializer
import eu.iamgio.quarkdown.context.Context
import eu.iamgio.quarkdown.context.MutableContext
import eu.iamgio.quarkdown.context.toc.TableOfContents
import eu.iamgio.quarkdown.document.DocumentInfo
import eu.iamgio.quarkdown.document.DocumentTheme
import eu.iamgio.quarkdown.document.DocumentType
import eu.iamgio.quarkdown.document.numbering.DocumentNumbering
import eu.iamgio.quarkdown.document.numbering.NumberingFormat
import eu.iamgio.quarkdown.document.page.PageMarginPosition
import eu.iamgio.quarkdown.document.page.PageOrientation
import eu.iamgio.quarkdown.document.page.PageSizeFormat
import eu.iamgio.quarkdown.document.size.Size
import eu.iamgio.quarkdown.document.size.Sizes
import eu.iamgio.quarkdown.function.reflect.annotation.Injected
import eu.iamgio.quarkdown.function.reflect.annotation.Name
import eu.iamgio.quarkdown.function.value.NodeValue
import eu.iamgio.quarkdown.function.value.OutputValue
import eu.iamgio.quarkdown.function.value.StringValue
import eu.iamgio.quarkdown.function.value.VoidValue
import eu.iamgio.quarkdown.function.value.wrappedAsValue
import eu.iamgio.quarkdown.localization.LocaleLoader
import eu.iamgio.quarkdown.pipeline.error.IOPipelineException

/**
 * `Document` stdlib module exporter.
 * This module handles document information and details.
 * @see eu.iamgio.quarkdown.document.DocumentInfo
 */
val Document: Module =
    setOf(
        ::docType,
        ::docName,
        ::docAuthor,
        ::docLanguage,
        ::theme,
        ::numbering,
        ::pageFormat,
        ::pageMarginContent,
        ::footer,
        ::currentPage,
        ::totalPages,
        ::autoPageBreak,
        ::disableAutoPageBreak,
        ::marker,
        ::tableOfContents,
    )

/**
 * If [value] is not `null`, it updates document information (according to [set]).
 * Document information is fetched from [this] context via [Context.documentInfo].
 * If it's `null`, the needed value (according to [get]) from the current document is returned.
 * @param value (optional) value to assign to a document info field
 * @return the result of [get], wrapped in a [StringValue], if [value] is `null`. [VoidValue] otherwise
 */
private fun <T> Context.modifyOrEchoDocumentInfo(
    value: T?,
    get: DocumentInfo.() -> String,
    set: DocumentInfo.(T) -> Unit,
): OutputValue<*> {
    if (value == null) {
        return get(this.documentInfo).wrappedAsValue()
    }

    set(this.documentInfo, value)
    return VoidValue
}

/**
 * If [type] is not `null`, it sets the document type to its value.
 * The document type affects its final output style.
 * If it's `null`, the name of the current document type is returned.
 * @param type (optional) type to assign to the document
 * @return the lowercase name of the current document type if [type] is `null`
 */
@Name("doctype")
fun docType(
    @Injected context: Context,
    type: DocumentType? = null,
): OutputValue<*> =
    context.modifyOrEchoDocumentInfo(
        type,
        get = { this.type.name.lowercase() },
        set = { this.type = it },
    )

/**
 * If [name] is not `null`, it sets the document name to its value.
 * If it's `null`, the current document name is returned.
 * @param name (optional) name to assign to the document
 * @return the current document name if [name] is `null`
 */
@Name("docname")
fun docName(
    @Injected context: Context,
    name: String? = null,
): OutputValue<*> =
    context.modifyOrEchoDocumentInfo(
        name,
        get = { this.name ?: "" },
        set = { this.name = it },
    )

/**
 * If [author] is not `null`, it sets the document author to its value.
 * If it's `null`, the current document author is returned.
 * @param author (optional) author to assign to the document
 * @return the current document author if [author] is `null`
 */
@Name("docauthor")
fun docAuthor(
    @Injected context: Context,
    author: String? = null,
): OutputValue<*> =
    context.modifyOrEchoDocumentInfo(
        author,
        get = { this.author ?: "" },
        set = { this.author = it },
    )

/**
 * If [locale] is not `null`, it sets the document locale to its value.
 * If it's `null`, the localized name of the current document locale is returned.
 * @param locale (optional) case-insensitive,
 *               either a locale tag (e.g. `en`, `en-US`, `it`, `fr-CA`)
 *               or an English name of a locale (e.g. `English`, `English (United States)`, `Italian`, `French (Canada)`)
 *               to assign to the document
 * @return the localized name of the current document locale if [locale] is `null`
 * @throws IllegalArgumentException if the locale tag is not invalid or not found
 */
@Name("doclang")
fun docLanguage(
    @Injected context: Context,
    locale: String? = null,
): OutputValue<*> =
    context.modifyOrEchoDocumentInfo(
        locale,
        get = { this.locale?.localizedName ?: "" },
        set = {
            this.locale =
                LocaleLoader.SYSTEM.find(it)
                    ?: throw IllegalArgumentException("Locale $it not found")
        },
    )

/**
 * Sets the global document theme.
 * @param color (optional) color scheme to assign (searched in `resources/render/theme/color`)
 * @param layout (optional) layout format to assign (searched in `resources/render/theme/layout`)
 * @throws IOPipelineException if any of the theme components isn't resolved
 */
fun theme(
    @Injected context: Context,
    color: String? = null,
    layout: String? = null,
): VoidValue {
    /**
     * @throws IOPipelineException if [theme] is not a valid theme
     */
    fun checkExistance(theme: String) {
        object {}.javaClass.getResource("/render/theme/${theme.lowercase()}.css")
            ?: throw IOPipelineException("Theme $theme not found")
    }

    // Update global theme.
    context.documentInfo.theme =
        DocumentTheme(
            color = color?.lowercase()?.also { checkExistance("color/$it") },
            layout = layout?.lowercase()?.also { checkExistance("layout/$it") },
        )

    return VoidValue
}

/**
 * Sets the global numbering format across the document.
 * Numbering is applied to elements that support it, such as headings and figures.
 *
 * - If a format is `none`, that kind of numbering is disabled.
 *
 * - Otherwise, it accepts a string where each character represents a symbol.
 *   Some characters are reserved for counting:
 *     - `1` for decimal (`1, 2, 3, ...`)
 *     - `a` for lowercase latin alphabet (`a, b, c, ...`)
 *     - `A` for uppercase latin alphabet (`A, B, C, ...`)
 *     - `i` for lowercase roman numerals (`i, ii, iii, ...`)
 *     - `I` for uppercase roman numerals (`I, II, III, ...`)
 *
 *     Any other character is considered a fixed symbol.
 *
 * Sample numbering strings are `1.1.1`, `1.A.a`, `A.A`.
 * @param headings numbering format string for headings (titles) and [tableOfContents] entries.
 * @param figures numbering format string for figures
 * @return [VoidValue]
 */
fun numbering(
    @Injected context: Context,
    headings: String? = null,
    figures: String? = null,
    tables: String? = null,
): VoidValue {
    fun parse(format: String): NumberingFormat =
        when (format) {
            // Disable numbering. Setting to null would instead trigger the default one.
            "none" -> NumberingFormat(symbols = emptyList())
            // Parse the format string.
            else -> NumberingFormat.fromString(format)
        }

    context.documentInfo.numbering =
        DocumentNumbering(
            headings = headings?.let(::parse),
            figures = figures?.let(::parse),
            tables = tables?.let(::parse),
        )

    return VoidValue
}

/**
 * Sets the format of the document.
 * If a value is `null`, the default value supplied by the underlying renderer is used.
 * If neither [format] nor [width] or [height] are `null`, the latter override the former.
 * If both [format] and [width] or [height] are `null`, the default value is used.
 * @param format standard size format of each page (overridden by [width] and [height])
 * @param orientation orientation of each page.
 *                    If not specified, the preferred orientation of the document type is used.
 *                    Does not take effect if [format] is not specified.
 * @param width width of each page
 * @param height height of each page
 * @param margin blank space around the content of each page. Only supported in paged documents
 * @param columns positive number of columns on each page.
 *                If set and greater than 1, the layout becomes multi-column. If < 1, the value is discarded
 */
@Name("pageformat")
fun pageFormat(
    @Injected context: Context,
    @Name("size") format: PageSizeFormat? = null,
    orientation: PageOrientation = context.documentInfo.type.preferredOrientation,
    width: Size? = null,
    height: Size? = null,
    margin: Sizes? = null,
    columns: Int? = null,
): VoidValue {
    with(context.documentInfo.pageFormat) {
        // If, for instance, the document is landscape and the given format is portrait,
        // the format is converted to landscape.
        val formatBounds = format?.getBounds(orientation)

        // Width and/or height override the format size if both are not null.
        this.pageWidth = width ?: formatBounds?.width ?: this.pageWidth
        this.pageHeight = height ?: formatBounds?.height ?: this.pageHeight

        this.margin = margin
        this.columnCount = columns?.takeIf { it > 0 }
    }

    return VoidValue
}

/**
 * Displays text content on each page of a document.
 * @param position position of the content within the page
 * @param content content to be displayed on each page
 * @return a wrapped [PageMarginContentInitializer] node
 */
@Name("pagemargin")
fun pageMarginContent(
    position: PageMarginPosition = PageMarginPosition.TOP_CENTER,
    content: MarkdownContent,
): NodeValue =
    PageMarginContentInitializer(
        content.children,
        position,
    ).wrappedAsValue()

/**
 * Displays text content on the bottom center of each page of a document.
 * Shortcut for [pageMarginContent] with [PageMarginPosition.BOTTOM_CENTER] as its position.
 * @see pageMarginContent
 * @return a wrapped [PageMarginContentInitializer] node with its position set to bottom center
 */
fun footer(content: MarkdownContent): NodeValue =
    pageMarginContent(
        PageMarginPosition.BOTTOM_CENTER,
        content,
    )

/**
 * Displays the index (beginning from 1) of the page this element lies in.
 * In case the current document type does not support page counting (e.g. plain document),
 * a placeholder is used.
 * @return a [PageCounter] node
 */
@Name("currentpage")
fun currentPage() = PageCounter(PageCounter.Target.CURRENT).wrappedAsValue()

/**
 * Displays the total amount of pages in the document.
 * In case the current document type does not support page counting (e.g. plain document),
 * a placeholder is used.
 * @return a [PageCounter] node
 */
@Name("totalpages")
fun totalPages() = PageCounter(PageCounter.Target.TOTAL).wrappedAsValue()

/**
 * Sets a new automatic page break threshold when a heading is found:
 * if a heading's depth value (the amount of leading `#`s) is equals or less than [maxDepth],
 * a page break is forced before the heading.
 * @param maxDepth heading depth to force page breaks for (positive only).
 * @throws IllegalArgumentException if [maxDepth] is a negative value
 * @see disableAutoPageBreak
 */
@Name("autopagebreak")
fun autoPageBreak(
    @Injected context: MutableContext,
    @Name("maxdepth") maxDepth: Int,
): VoidValue {
    if (maxDepth < 0) {
        throw IllegalArgumentException("Heading depth cannot be negative.")
    }

    context.options.autoPageBreakHeadingMaxDepth = maxDepth
    return VoidValue
}

/**
 * Disables automatic page breaks when a heading is found.
 * @see autoPageBreak
 */
@Name("noautopagebreak")
fun disableAutoPageBreak(
    @Injected context: MutableContext,
) = autoPageBreak(context, 0)

/**
 * Creates an invisible marker that points to a specific location in the document,
 * and can be referenced by other elements as would happen with a regular heading.
 * It can be particularly useful when using a table of contents.
 * @param name name of the marker
 * @return a wrapped [Heading] marker node
 * @see tableOfContents
 */
fun marker(name: InlineMarkdownContent) = Heading.marker(name.children).wrappedAsValue()

/**
 * Generates a table of contents for the document.
 * @param title title of the table of contents. If unset, the default title is used
 * @param maxDepth maximum depth of the table of contents
 * @param focusedItem if set, adds focus to the item of the table of contents with the same text content as this argument.
 *                    Inline style (strong, emphasis, etc.) is ignored when comparing the text content.
 * @return a wrapped [TableOfContents] node
 */
@Name("tableofcontents")
fun tableOfContents(
    title: InlineMarkdownContent? = null,
    @Name("maxdepth") maxDepth: Int = 3,
    @Name("focus") focusedItem: InlineMarkdownContent? = null,
): NodeValue =
    TableOfContentsView(
        title?.children,
        maxDepth,
        focusedItem?.children,
    ).wrappedAsValue()
