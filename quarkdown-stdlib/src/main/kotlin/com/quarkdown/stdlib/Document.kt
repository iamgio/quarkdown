package com.quarkdown.stdlib

import com.quarkdown.core.ast.InlineMarkdownContent
import com.quarkdown.core.ast.MarkdownContent
import com.quarkdown.core.ast.base.block.Heading
import com.quarkdown.core.ast.quarkdown.block.Container
import com.quarkdown.core.ast.quarkdown.block.NavigationContainer
import com.quarkdown.core.ast.quarkdown.block.toc.TableOfContentsView
import com.quarkdown.core.ast.quarkdown.inline.LastHeading
import com.quarkdown.core.ast.quarkdown.inline.PageCounter
import com.quarkdown.core.ast.quarkdown.invisible.PageMarginContentInitializer
import com.quarkdown.core.ast.quarkdown.invisible.PageNumberReset
import com.quarkdown.core.context.Context
import com.quarkdown.core.context.MutableContext
import com.quarkdown.core.context.toc.TableOfContents
import com.quarkdown.core.document.DocumentAuthor
import com.quarkdown.core.document.DocumentInfo
import com.quarkdown.core.document.DocumentTheme
import com.quarkdown.core.document.DocumentType
import com.quarkdown.core.document.deepCopy
import com.quarkdown.core.document.layout.caption.CaptionPosition
import com.quarkdown.core.document.layout.caption.CaptionPositionInfo
import com.quarkdown.core.document.layout.caption.merge
import com.quarkdown.core.document.layout.font.FontInfo
import com.quarkdown.core.document.layout.page.PageFormatInfo
import com.quarkdown.core.document.layout.page.PageMarginPosition
import com.quarkdown.core.document.layout.page.PageOrientation
import com.quarkdown.core.document.layout.page.PageSizeFormat
import com.quarkdown.core.document.layout.page.merge
import com.quarkdown.core.document.layout.paragraph.ParagraphStyleInfo
import com.quarkdown.core.document.layout.paragraph.merge
import com.quarkdown.core.document.numbering.DocumentNumbering
import com.quarkdown.core.document.numbering.NumberingFormat
import com.quarkdown.core.document.numbering.merge
import com.quarkdown.core.document.size.Size
import com.quarkdown.core.document.size.Sizes
import com.quarkdown.core.document.tex.TexInfo
import com.quarkdown.core.function.library.module.QuarkdownModule
import com.quarkdown.core.function.library.module.moduleOf
import com.quarkdown.core.function.reflect.annotation.Injected
import com.quarkdown.core.function.reflect.annotation.LikelyBody
import com.quarkdown.core.function.reflect.annotation.LikelyNamed
import com.quarkdown.core.function.reflect.annotation.Name
import com.quarkdown.core.function.reflect.annotation.NotForDocumentType
import com.quarkdown.core.function.value.DictionaryValue
import com.quarkdown.core.function.value.DynamicValue
import com.quarkdown.core.function.value.NodeValue
import com.quarkdown.core.function.value.OutputValue
import com.quarkdown.core.function.value.StringValue
import com.quarkdown.core.function.value.Value
import com.quarkdown.core.function.value.VoidValue
import com.quarkdown.core.function.value.dictionaryOf
import com.quarkdown.core.function.value.wrappedAsValue
import com.quarkdown.core.localization.LocaleLoader
import com.quarkdown.core.misc.color.Color
import com.quarkdown.core.misc.font.FontFamily
import com.quarkdown.core.pipeline.error.IOPipelineException
import com.quarkdown.stdlib.internal.loadFontFamily

/**
 * `Document` stdlib module exporter.
 * This module handles document information and details.
 * @see com.quarkdown.core.document.DocumentInfo
 */
val Document: QuarkdownModule =
    moduleOf(
        ::docType,
        ::docName,
        ::docDescription,
        ::docAuthor,
        ::docAuthors,
        ::docKeywords,
        ::docLanguage,
        ::theme,
        ::numbering,
        ::disableNumbering,
        ::font,
        ::paragraphStyle,
        ::captionPosition,
        ::texMacro,
        ::pageFormat,
        ::pageMarginContent,
        ::footer,
        ::resetPageNumber,
        ::currentPage,
        ::totalPages,
        ::lastHeading,
        ::autoPageBreak,
        ::disableAutoPageBreak,
        ::marker,
        ::navigationContainer,
        ::tableOfContents,
    )

/**
 * If [value] is not `null`, it updates document information (according to [modify]).
 * Document information is fetched from [this] context via [Context.documentInfo].
 * If it's `null`, the needed value (according to [get]) from the current document is returned.
 * @param value (optional) value to assign to a document info field
 * @param get function to fetch the needed value from [DocumentInfo] if [value] is `null`
 * @param modify function to modify [DocumentInfo] if [value] is not `null` (returns the modified copy)
 * @return the result of [get], wrapped in a [StringValue], if [value] is `null`. [VoidValue] otherwise
 */
private fun <T> MutableContext.modifyOrEchoDocumentInfo(
    value: T?,
    get: DocumentInfo.() -> OutputValue<*>,
    modify: DocumentInfo.(T) -> DocumentInfo,
): OutputValue<*> {
    if (value == null) {
        return get(this.documentInfo)
    }

    this.documentInfo = modify(this.documentInfo, value)
    return VoidValue
}

/**
 * If [type] is specified, sets the document type to that value.
 * The document type affects its final output style, numbering format and several other properties.
 *
 * ```
 * .doctype {paged}
 * ```
 *
 * If it's unset, the lowecase name of the current document type is returned.
 *
 * ```
 * The current document type is .doctype
 * ```
 *
 * @param type optional type to assign to the document
 * @return the lowercase name of the current document type if [type] is unset, nothing otherwise
 * @wiki Document types
 */
@Name("doctype")
fun docType(
    @Injected context: MutableContext,
    type: DocumentType? = null,
): OutputValue<*> =
    context.modifyOrEchoDocumentInfo(
        type,
        get = {
            this.type.name
                .lowercase()
                .wrappedAsValue()
        },
        modify = { copy(type = it) },
    )

/**
 * If [name] is specified, sets the document name to that value.
 * The document name affects the name of the output file.
 *
 * ```
 * .docname {My document}
 * ```
 *
 * If it's unset, the current name of the document is returned.
 *
 * ```
 * The current document name is .docname
 * ```
 *
 * @param name optional non-blank name to assign to the document
 * @return the current document name if [name] is unset, nothing otherwise
 * @throws IllegalArgumentException if [name] is blank
 * @wiki Document metadata
 */
@Name("docname")
fun docName(
    @Injected context: MutableContext,
    name: String? = null,
): OutputValue<*> =
    context.modifyOrEchoDocumentInfo(
        name,
        get = { (this.name ?: "").wrappedAsValue() },
        modify = {
            require(it.isNotBlank()) { "Document name cannot be blank." }
            copy(name = it)
        },
    )

/**
 * If [description] is specified, sets the document description to that value.
 *
 * In HTML, descriptions are exported to SEO-friendly meta tags.
 *
 * ```
 * .docdescription {This is a sample document}
 * ```
 *
 * If it's unset, the current description of the document is returned.
 *
 * ```
 * The current document description is .docdescription
 * ```
 *
 * @param description optional description to assign to the document
 * @return the current document description if [description] is unset, nothing otherwise
 * @wiki Document metadata
 */
@Name("docdescription")
fun docDescription(
    @Injected context: MutableContext,
    description: String? = null,
): OutputValue<*> =
    context.modifyOrEchoDocumentInfo(
        description,
        get = { (this.description ?: "").wrappedAsValue() },
        modify = { copy(description = it) },
    )

/**
 * If [author] is specified, sets the document author to that value.
 * This is a shortcut for [docAuthors] when there's only one author without additional information.
 *
 * ```
 * .docauthor {John Doe}
 * ```
 *
 * If it's unset, the current author of the document is returned.
 *
 * If the authors were set via [docAuthors], only the name of the first author is returned.
 * If you are looking forward to iterating over all authors, use [forEach] over [docAuthors] instead.
 *
 * ```
 * The current document author is .docauthor
 * ```
 *
 * @param author optional author name to assign to the document
 * @return the current document author if [author] is unset, nothing otherwise
 * @wiki Document metadata
 */
@Name("docauthor")
fun docAuthor(
    @Injected context: MutableContext,
    author: String? = null,
): OutputValue<*> =
    context.modifyOrEchoDocumentInfo(
        author,
        get = { (this.authors.firstOrNull()?.name ?: "").wrappedAsValue() },
        modify = { copy(authors = authors + DocumentAuthor(name = it)) },
    )

/**
 * If [authors] is specified, sets the document authors to that value.
 * Compared to [docAuthor], this function allows for multiple authors and additional information.
 *
 * ```
 * .docauthors
 *   - John Doe
 *     - email: johndoe@email.com
 *     - website: https://github.com/iamgio/quarkdown
 *   - Jane Doe
 *     - email: janedoe@email.com
 * ```
 *
 * If it's unset, the current authors of the document are returned as a dictionary,
 * where each key is the author's name, and its value is another dictionary containing the additional information.
 *
 * The following example takes advantage of [forEach] with destructuring to iterate over all authors and their information.
 *
 * ```
 * .foreach {.docauthors}
 *     name info:
 *     .name, .get {email} from:{.info}
 * ```
 *
 * > Output:
 * >
 * > John Doe, johndoe@email.com
 * >
 * > Jane Doe, janedoe@email.com
 *
 * @param authors optional authors to assign to the document.
 *                Each dictionary entry contains the author's name associated with a nested dictionary of additional information.
 * @return the current document authors if [authors] is unset, nothing otherwise
 * @wiki Document metadata
 */
@Name("docauthors")
fun docAuthors(
    @Injected context: MutableContext,
    authors: Map<String, DictionaryValue<OutputValue<String>>>? = null,
): OutputValue<*> =
    context.modifyOrEchoDocumentInfo(
        authors,
        get = {
            // List<(String, Map<String, String>)> -> Map<String, Map<String, String>>
            dictionaryOf(
                this.authors.map {
                    it.name to
                        DictionaryValue(
                            it.info.mapValues { (_, value) -> value.wrappedAsValue() }.toMutableMap(),
                        )
                },
            )
        },
        modify = {
            // Map<String, Map<String, String>> -> List<(String, Map<String, String>)>
            val authors =
                this.authors +
                    it.map { (name, info) ->
                        DocumentAuthor(
                            name = name,
                            info = info.unwrappedValue.mapValues { (_, value) -> value.unwrappedValue },
                        )
                    }
            copy(authors = authors)
        },
    )

/**
 * If [keywords] is specified, sets the document keywords to those values.
 * In HTML, keywords are exported to SEO-friendly meta tags.
 *
 * ```
 * .dockeywords
 *   - quarkdown
 *   - markdown
 *   - documentation
 * ```
 *
 * If it's unset, the current keywords of the document are returned as a list.
 *
 * ```
 * .foreach {.dockeywords}
 *     keyword:
 *     .keyword
 * ```
 *
 * @param keywords optional collection of keywords to assign to the document
 * @return the current document keywords if [keywords] is unset, nothing otherwise
 * @wiki Document metadata
 */
@Name("dockeywords")
fun docKeywords(
    @Injected context: MutableContext,
    @LikelyBody keywords: Iterable<DynamicValue>? = null,
): OutputValue<*> =
    context.modifyOrEchoDocumentInfo(
        keywords,
        get = { this.keywords.map(::StringValue).wrappedAsValue() },
        modify = {
            copy(keywords = it.map { value -> value.unwrappedValue.toString() }.toList())
        },
    )

/**
 * If [locale] is specified, sets the document language to that value.
 * The document language affects localization ([localization], [localize]), hyphenation and other locale-specific properties.
 *
 * For a list of supported locales for built-in localizations, see [here](https://github.com/iamgio/quarkdown/wiki/localization).
 *
 * ```
 * .doclang {en}
 * ```
 *
 * or
 *
 * ```
 * .doclang {English}
 * ```
 *
 * If it's unset, the current language of the document is returned as its localized name (e.g. `English`, `Italiano`, `Français`).
 *
 * ```
 * The current document language is .doclang
 * ```
 *
 * @param locale optional, case-insensitive,
 *               either a locale tag (e.g. `en`, `en-US`, `it`, `fr-CA`)
 *               or an English name of a locale (e.g. `English`, `English (United States)`, `Italian`, `French (Canada)`)
 *               to assign to the document
 * @return the localized name of the current document language if [locale] is unset, nothing otherwise
 * @throws IllegalArgumentException if the locale tag is invalid or not found
 * @wiki Document metadata
 */
@Name("doclang")
fun docLanguage(
    @Injected context: MutableContext,
    locale: String? = null,
): OutputValue<*> =
    context.modifyOrEchoDocumentInfo(
        locale,
        get = { (this.locale?.localizedName ?: "").wrappedAsValue() },
        modify = {
            copy(
                locale =
                    LocaleLoader.SYSTEM.find(it)
                        ?: throw IllegalArgumentException("Locale $it not found"),
            )
        },
    )

/**
 * Sets the document theme.
 *
 * The two components of a theme are:
 * - Color themes, which define the color scheme of a document, including colors for text, backgrounds, and other elements.
 * - Layout themes, which define the general structural rules of the layout, including margins, spacing, and positioning.
 *
 * If any of the components isn't specified, the current one is kept, or the default one is used if not set yet.
 *
 * Check out the wiki page for a list of available themes.
 *
 * @param color optional color scheme to assign
 * @param layout layout format to assign
 * @throws IOPipelineException if any of the theme components isn't resolved
 * @wiki Themes
 */
fun theme(
    @Injected context: MutableContext,
    color: String? = null,
    @LikelyNamed layout: String? = null,
): VoidValue {
    /**
     * @throws IOPipelineException if [theme] is not a valid theme
     */
    fun checkExistance(theme: String) {
        object {}.javaClass.getResource("/render/theme/${theme.lowercase()}.css")
            ?: throw IOPipelineException("Theme $theme not found")
    }

    val theme =
        DocumentTheme(
            color = color?.lowercase()?.also { checkExistance("color/$it") },
            layout = layout?.lowercase()?.also { checkExistance("layout/$it") },
        )

    // Update global theme.
    context.documentInfo = context.documentInfo.copy(theme = theme)

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
 *
 * ```yaml
 * .numbering
 *     - headings: 1.1
 *     - figures: 1.a
 * ```
 *
 * If this function is *not* called, the default numbering format is picked depending on the document type.
 *
 * @param merge if true, merges the given formats with the current ones (including defaults),
 * so that unspecified formats are kept unchanged.
 * If false, completely overrides the current formats, so that unspecified formats are disabled.
 * @param formats dictionary of numbering formats for different element types.
 * Built-in keys are:
 * - `headings`, used for headings (titles) and [tableOfContents] entries;
 * - `figures`, used for captioned images;
 * - `tables`, used for captioned tables;
 * - `equations`, used for math blocks;
 * - `code`, used for code blocks;
 * - `footnotes`, used for footnotes and references to them.
 * Any other key can be addressed by custom elements (see [numbered]).
 * @wiki Numbering
 */
fun numbering(
    @Injected context: MutableContext,
    @LikelyNamed merge: Boolean = true,
    @LikelyBody formats: Map<String, Value<String>>,
): VoidValue {
    fun parse(format: Value<String>): NumberingFormat =
        when (val unwrapped = format.unwrappedValue) {
            // Disable numbering. Setting to null would instead trigger the default one.
            "none" -> NumberingFormat(symbols = emptyList())

            // Parse the format string.
            else -> NumberingFormat.fromString(unwrapped)
        }

    val numbering =
        DocumentNumbering(
            headings = formats["headings"]?.let(::parse),
            figures = formats["figures"]?.let(::parse),
            tables = formats["tables"]?.let(::parse),
            math = formats["equations"]?.let(::parse),
            codeBlocks = formats["code"]?.let(::parse),
            footnotes = formats["footnotes"]?.let(::parse),
            extra = formats.map { (key, value) -> key to parse(value) }.toMap(),
        )

    context.documentInfo =
        context.documentInfo.copy(
            numbering =
                if (merge) {
                    numbering.merge(context.documentInfo.numberingOrDefault)
                } else {
                    numbering
                },
        )

    return VoidValue
}

/**
 * Disables numbering across the document, in case a default numbering is set by either [numbering] or the document type default.
 * @see numbering
 * @wiki Numbering
 */
@Name("nonumbering")
fun disableNumbering(
    @Injected context: MutableContext,
) = numbering(context, merge = false, formats = emptyMap())

/**
 * Updates the global font configuration of the document.
 *
 * Font families can be loaded from any of the following sources:
 * - From file (e.g. `path/to/font.ttf`)
 * - From URL (e.g. `https://example.com/font.ttf`)
 * - From system fonts (e.g. `Arial`, `Times New Roman`)
 * - From Google Fonts (e.g. `GoogleFonts:Roboto`).
 *
 * Local and remote font resources are processed by the [media storage](https://github.com/iamgio/quarkdown/wiki/media-storage).
 * This means, for instance, HTML output will carry local fonts into the output directory for increased portability.
 *
 * This function can be called multiple times to add multiple font configurations.
 * Latter configurations have higher priority. This can be useful to specify fallback fonts for different glyphs.
 * The following example applies the `Inter` font for Latin characters, and the `Ma Shan Zheng` font for Chinese characters.
 * This happens because `Inter` does not support Chinese glyphs, so the renderer falls back to the next configuration.
 *
 * ```
 * .font {GoogleFonts:Ma Shan Zheng}
 * .font {GoogleFonts:Inter}
 * ```
 *
 * The [size] parameter does not stack instead, and only the last specified size is used among all configurations.
 *
 * @param main main font family of regular content: paragraphs, lists, blockquotes, tables and other textual elements
 * @param heading font family of headings.
 *                If unset, [main] is applied to headings too
 *                only if the layout [theme] does not enforce a specific heading font
 * @param code font family of code blocks and code spans
 * @param size main font size of the text on each page. Other elements, such as headings, will scale accordingly
 * @wiki Font configuration
 */
fun font(
    @Injected context: MutableContext,
    main: String? = null,
    @LikelyNamed heading: String? = null,
    @LikelyNamed code: String? = null,
    @LikelyNamed size: Size? = null,
): VoidValue {
    fun fontFamily(name: String?): FontFamily? = name?.let { loadFontFamily(it, context) }

    val fontInfo =
        FontInfo(
            mainFamily = fontFamily(main),
            headingFamily = fontFamily(heading),
            codeFamily = fontFamily(code),
            size = size,
        )

    // Update global font info.
    context.documentInfo = context.documentInfo.deepCopy(layoutFonts = context.documentInfo.layout.fonts + fontInfo)

    return VoidValue
}

/**
 * Sets the global style of paragraphs in the document.
 * If a value is unset, the default value supplied by the underlying renderer is used.
 *
 * The default values may also be affected by the current document locale, set via [docLanguage].
 * For instance, the Chinese `zh` locale prefers a 2em indentation and no vertical spacing by default.
 *
 * @param lineHeight height of each line, multiplied by the font size
 * @param letterSpacing whitespace between letters, multiplied by the font size
 * @param spacing whitespace between paragraphs, multiplied by the font size.
 *                This also minorly affects whitespace around lists and between list items
 * @param indent whitespace at the start of each paragraph, multiplied by the font size.
 *               LaTeX's policy is used: indenting the first line of paragraphs, except the first one and aligned ones
 * @wiki Paragraph style
 */
@Name("paragraphstyle")
fun paragraphStyle(
    @Injected context: MutableContext,
    @Name("lineheight") lineHeight: Number? = null,
    @Name("letterspacing") letterSpacing: Number? = null,
    @LikelyNamed spacing: Number? = null,
    @LikelyNamed indent: Number? = null,
): VoidValue {
    val currentStyle = context.documentInfo.layout.paragraphStyle

    val style =
        ParagraphStyleInfo(
            lineHeight = lineHeight?.toDouble(),
            letterSpacing = letterSpacing?.toDouble(),
            spacing = spacing?.toDouble(),
            indent = indent?.toDouble(),
        )

    // Update global paragraph style.
    context.documentInfo = context.documentInfo.deepCopy(layoutParagraphStyle = style.merge(currentStyle))

    return VoidValue
}

/**
 * Sets the position of captions, relative to the content they describe.
 * @param default the default position for all captions. Defaults to bottom
 * @param figures caption position for figures. If set, overrides [default] for figures
 * @param tables caption position for tables. If set, overrides [default] for tables
 * @param codeBlocks caption position for code blocks. If set, overrides [default] for code blocks
 * @wiki Caption position
 */
@Name("captionposition")
fun captionPosition(
    @Injected context: MutableContext,
    @LikelyNamed default: CaptionPosition? = null,
    @LikelyNamed figures: CaptionPosition? = null,
    @LikelyNamed tables: CaptionPosition? = null,
    @Name("code") codeBlocks: CaptionPosition? = null,
): VoidValue {
    val currentPosition = context.documentInfo.layout.captionPosition
    val position =
        CaptionPositionInfo(
            default = default ?: currentPosition.default,
            figures = figures,
            tables = tables,
            codeBlocks = codeBlocks,
        )

    // Update global caption position.
    context.documentInfo = context.documentInfo.deepCopy(layoutCaptionPosition = position.merge(currentPosition))

    return VoidValue
}

/**
 * Creates a new global TeX macro that can be accessed within math blocks.
 *
 * ```
 * .texmacro {\R}
 *     \mathbb{R}
 * ```
 *
 * @param name name of the macro
 * @param macro TeX code
 * @wiki TeX macros
 */
@Name("texmacro")
fun texMacro(
    @Injected context: MutableContext,
    name: String,
    @LikelyBody macro: String,
) = VoidValue.also {
    val texInfo: TexInfo =
        context.documentInfo.tex.copy(
            macros =
                context.documentInfo.tex.macros + (name to macro),
        )

    context.documentInfo = context.documentInfo.copy(tex = texInfo)
}

/**
 * Sets the page layout format of the document.
 * If a value is unset, the default value supplied by the underlying renderer is used.
 *
 * - In case of `paged` documents, this function defines the properties of each page.
 * - In case of `slides` documents, this function defines the properties of each slide.
 * - In case of `plain` documents, this function defines some properties of the whole document, seeing it as just one page.
 *   Not all effects of this function are supported in plain documents.
 *
 * If both [format] and [width] or [height] are set, the latter overrides the former.
 * If both [format] and [width] or [height] are unset, the default value is used.
 *
 * If any of [borderTop], [borderRight], [borderBottom], [borderLeft] or [borderColor] is set,
 * the border will be applied around the content area of each page.
 * If only [borderColor] is set, the border will be applied with a default width to each side.
 * Border is not supported in plain documents.
 *
 * @param format standard size format of each page (overridden by [width] and [height])
 * @param orientation orientation of each page.
 *                    If not specified, the preferred orientation of the document type is used.
 *                    Does not take effect if [format] is not specified.
 * @param width width of each page
 * @param height height of each page
 * @param margin blank space around the content of each page. Not supported in slides documents
 * @param borderTop border width of the top content area of each page
 * @param borderRight border width of the right content area of each page
 * @param borderBottom border width of the bottom content area of each page
 * @param borderLeft border width of the left content area of each page
 * @param borderColor color of the border around the content area of each page
 * @param columns positive number of columns on each page.
 *                If set and greater than 1, the layout becomes multi-column. If < 1, the value is discarded
 * @param alignment text alignment of the content on each page
 * @wiki Page format
 */
@Name("pageformat")
fun pageFormat(
    @Injected context: MutableContext,
    @Name("size") format: PageSizeFormat? = null,
    @LikelyNamed orientation: PageOrientation = context.documentInfo.type.preferredOrientation,
    @LikelyNamed width: Size? = null,
    @LikelyNamed height: Size? = null,
    @LikelyNamed margin: Sizes? = null,
    @Name("bordertop") borderTop: Size? = null,
    @Name("borderright") borderRight: Size? = null,
    @Name("borderbottom") borderBottom: Size? = null,
    @Name("borderleft") borderLeft: Size? = null,
    @Name("bordercolor") borderColor: Color? = null,
    @LikelyNamed columns: Int? = null,
    @LikelyNamed alignment: Container.TextAlignment? = null,
): VoidValue {
    val currentFormat = context.documentInfo.layout.pageFormat

    // If, for instance, the document is landscape and the given format is portrait,
    // the format is converted to landscape.
    val formatBounds = format?.getBounds(orientation)

    // Whether at least one border property is set.
    val hasBorder = borderTop != null || borderRight != null || borderBottom != null || borderLeft != null

    val format =
        PageFormatInfo(
            pageWidth = width ?: formatBounds?.width,
            pageHeight = height ?: formatBounds?.height,
            margin = margin,
            columnCount = columns?.takeIf { it > 0 },
            alignment = alignment,
            contentBorderWidth =
                Sizes(
                    top = borderTop ?: currentFormat.contentBorderWidth?.top ?: Size.ZERO,
                    right = borderRight ?: currentFormat.contentBorderWidth?.right ?: Size.ZERO,
                    bottom = borderBottom ?: currentFormat.contentBorderWidth?.bottom ?: Size.ZERO,
                    left = borderLeft ?: currentFormat.contentBorderWidth?.left ?: Size.ZERO,
                ).takeIf { hasBorder },
            contentBorderColor = borderColor,
        )

    // Update global page format.
    context.documentInfo = context.documentInfo.deepCopy(layoutPageFormat = format.merge(currentFormat))

    return VoidValue
}

/**
 * Displays content on each page of a document.
 *
 * - In case of `paged` documents, the content is displayed in a dedicated area on each page.
 * - In case of `slides` documents, the content is displayed on each slide, not in a dedicated area.
 * - In case of `plain` documents, the content is displayed in a fixed position (not affected by scrolling).
 *
 * @param position position of the content within the page
 * @param content content to be displayed on each page
 * @return a [PageMarginContentInitializer] node
 * @wiki Page margin content
 */
@Name("pagemargin")
fun pageMarginContent(
    position: PageMarginPosition,
    @LikelyBody content: MarkdownContent,
): NodeValue =
    PageMarginContentInitializer(
        content.children,
        position,
    ).wrappedAsValue()

/**
 * Displays content on the bottom center of each page of a document.
 *
 * This is a shortcut for [pageMarginContent] with `bottomcenter` as its position.
 * Some themes may style footers differently than other page margin content.
 *
 * @param content content to be displayed on each page as a footer
 * @return a [PageMarginContentInitializer] node with its position set to bottom center
 * @see pageMarginContent
 * @wiki Page margin content
 */
fun footer(
    @LikelyBody content: MarkdownContent,
): NodeValue =
    pageMarginContent(
        PageMarginPosition.BOTTOM_CENTER,
        content,
    )

/**
 * Displays the index (starting from 1) of the page this element lies in.
 *
 * In case the current document type does not support page counting (e.g. `plain` documents), `-` is displayed instead.
 *
 * @return a new [PageCounter] node
 * @wiki Page counter
 */
@Name("currentpage")
fun currentPage() = PageCounter(PageCounter.Target.CURRENT).wrappedAsValue()

/**
 * Displays the total amount of pages in the document.
 *
 * In case the current document type does not support page counting (e.g. `plain` documents), `-` is displayed instead.
 *
 * @return a new [PageCounter] node
 * @wiki Page counter
 */
@Name("totalpages")
fun totalPages() = PageCounter(PageCounter.Target.TOTAL).wrappedAsValue()

/**
 * Resets the logical page number counter.
 *
 * The page that contains this command is assigned [startFrom] as its displayed number.
 * This affects features that read page numbers, such as [currentPage].
 *
 * ```
 * .resetpagenumber start:{5}
 * ```
 *
 * @param startFrom page number to assign to the page where this function appears
 */
@Name("resetpagenumber")
fun resetPageNumber(
    @Name("start") startFrom: Int = 1,
): NodeValue = PageNumberReset(startFrom).wrappedAsValue()

/**
 * Displays the last heading, of the given [depth], encountered in the current page.
 *
 * ```markdown
 * # The heading
 *
 * .lastheading depth:{1} <!-- Displays "The heading" -->
 * ```
 *
 * If the current page does not contain any, the last heading of the previous page is used instead, continuing backwards until a heading is found.
 * If, ultimately, no such heading is found in the whole document, nothing is displayed.
 *
 * ```markdown
 * <!-- Page 1 -->
 *
 * # Heading 1
 *
 * ## Heading 2
 *
 * <!-- Page 2 -->
 *
 * .lastheading depth:{2} <!-- Displays "Heading 2" -->
 * ```
 *
 * This can be particularly useful in combination with [pageMarginContent], to show the current section of the document:
 *
 * ```
 * .pagemargin {bottomcenter}
 *     .lastheading depth:{1}
 * ```
 *
 * Note that encountering a heading of lesser depth than [depth] resets the current last heading.
 * For instance:
 *
 * ```markdown
 * <!-- Page 1 -->
 *
 * # Heading 1
 *
 * ## Heading 2
 *
 * <!-- Page 2 -->
 *
 * # Heading 3
 *
 * .lastheading depth:{2} <!-- Empty -->
 * ```
 *
 * @param depth the depth of the last [Heading] to match (1-6)
 * @return a new [LastHeading] node
 * @see pageMarginContent
 * @wiki Persistent headings
 */
@NotForDocumentType(DocumentType.PLAIN)
@Name("lastheading")
fun lastHeading(depth: Int) = LastHeading(depth).wrappedAsValue()

/**
 * Sets a new automatic page break threshold when a heading is found:
 * if a heading's depth value (the amount of leading `#`s) is equal to or less than [maxDepth],
 * a page break is forced before the heading.
 *
 * If this function is *not* called, automatic page breaks are set depending on the document type.
 * For instance, `paged` documents have automatic page breaks enabled for headings of depth `1` by default.
 *
 * Any page break can be disabled via [disableAutoPageBreak] (or by setting [maxDepth] to `0`).
 *
 * @param maxDepth heading depth to force page breaks for (positive only).
 * @throws IllegalArgumentException if [maxDepth] is a negative value
 * @see disableAutoPageBreak
 * @wiki Page break
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
 * @wiki Page break
 */
@Name("noautopagebreak")
fun disableAutoPageBreak(
    @Injected context: MutableContext,
) = autoPageBreak(context, 0)

/**
 * Creates an invisible marker, that points to a specific location in the document,
 * and can be referenced by other elements as would happen with a regular heading.
 *
 * It can be particularly useful when using a table of contents.
 *
 * @param name name of the marker
 * @return a [Heading] marker node
 * @see tableOfContents
 * @wiki Table of contents
 */
fun marker(name: InlineMarkdownContent) = Heading.marker(name.children).wrappedAsValue()

/**
 * Creates a navigation container, which marks its content as a navigable section.
 *
 * This doesn't affect the layout of the document by itself, but rather brings semantic meaning that
 * can be used by themes and renderers to provide additional navigation features, styling, behaviors and accessibility.
 *
 * For example, this can be useful in `docs` documents to mark sidebar navigation sections,
 * in combination with [pageMarginContent], in order to list links to subdocuments:
 *
 * ```
 * .pagemargin {lefttop}
 *     .navigation role:{pagelist}
 *         - [Page 1](page-1.qd)
 *         - [Page 2](page-2.qd)
 * ```
 *
 * @param role role of the navigation container
 * @param content content of the container
 * @return a [NavigationContainer] node
 */
@Name("navigation")
fun navigationContainer(
    role: NavigationContainer.Role? = null,
    @LikelyBody content: MarkdownContent,
): NodeValue =
    NavigationContainer(
        role,
        content.children,
    ).wrappedAsValue()

/**
 * Generates a table of contents, based on the headings in the document,
 * organized in a hierarchical structure defined by each heading's depth.
 *
 * @param title title of the table of contents. If unset, the default localized title is used. If blank, no title is displayed.
 * @param maxDepth maximum depth of the table of contents.
 *                 Only headings with a depth (number of leading `#`s) equal to or less than this value are included.
 * @param includeUnnumbered if enabled, unnumbered (decorative) headings are also included in the table of contents.
 *                          By default, only numbered headings are included.
 * @param focusedItem if set, adds focus to the item of the table of contents with the same text content as this argument.
 *                    Inline style (strong, emphasis, etc.) is ignored when comparing the text content.
 *                    When at least one item is focused, non-focused items are visually de-emphasized.
 * @return a [TableOfContents] node
 * @wiki Table of contents
 */
@Name("tableofcontents")
fun tableOfContents(
    @LikelyNamed title: InlineMarkdownContent? = null,
    @Name("maxdepth") maxDepth: Int = 3,
    @Name("includeunnumbered") includeUnnumbered: Boolean = false,
    @Name("focus") focusedItem: InlineMarkdownContent? = null,
): NodeValue =
    TableOfContentsView(
        title?.children,
        maxDepth,
        includeUnnumbered,
        focusedItem?.children,
    ).wrappedAsValue()
