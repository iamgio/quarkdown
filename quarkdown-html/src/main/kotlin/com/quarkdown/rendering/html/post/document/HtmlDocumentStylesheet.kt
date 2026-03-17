package com.quarkdown.rendering.html.post.document

import com.quarkdown.core.context.Context
import com.quarkdown.core.document.DocumentType
import com.quarkdown.core.document.layout.font.FontInfo
import com.quarkdown.core.document.layout.page.PageFormatInfo
import com.quarkdown.core.document.layout.paragraph.ParagraphStyleInfo
import com.quarkdown.core.misc.font.FontFamily
import com.quarkdown.rendering.html.css.asCSS
import com.quarkdown.rendering.html.css.stylesheet

/**
 * Generates the CSS `<style>` content for an HTML document, including font-face imports,
 * CSS custom properties, and layout rules derived from the document's configuration.
 *
 * This class encapsulates all CSS/font concerns, keeping the [HtmlDocumentBuilder] focused
 * purely on HTML structure.
 *
 * @param context the rendering context containing document metadata, layout, and font configuration
 */
class HtmlDocumentStylesheet(
    private val context: Context,
    private val pageFormats: List<PageFormatInfo>,
) {
    private val document = context.documentInfo

    fun build(): String =
        buildString {
            appendLine(buildFonts())
            appendLine(buildParagraphStyle(document.layout.paragraphStyle))

            pageFormats.forEach { pageFormat ->
                appendLine(buildPageFormat(pageFormat))
            }
        }

    /**
     * Builds `@font-face` declarations and font CSS custom properties.
     * Emitted once, outside the per-format loop, since fonts are document-global
     * and CSS custom properties are invalid inside `@page` at-rules.
     */
    private fun buildFonts(): String =
        stylesheet {
            raw(fontFaceSnippets().joinToString("\n"))

            rule("body") {
                "--qd-main-custom-font" value mainFontFamily()
                "--qd-heading-custom-font" value headingFontFamily()
                "--qd-code-custom-font" value codeFontFamily()
                "--qd-main-font-size" value fontSizeCss()
            }
        }

    private fun buildParagraphStyle(paragraphStyle: ParagraphStyleInfo): String =
        stylesheet {
            rule("body") {
                "--qd-line-height" value paragraphStyle.lineHeight?.toString()
                "--qd-letter-spacing" value paragraphStyle.letterSpacing?.let { "${it}em" }
                "--qd-paragraph-vertical-margin" value paragraphStyle.spacing?.let { "${it}em" }
            }
            rule("p") {
                "--qd-paragraph-text-indent" value paragraphStyle.indent?.let { "${it}em" }
            }
        }

    /**
     * Builds the CSS stylesheet for a single [PageFormatInfo], depending on its scope and properties.
     * When [PageFormatInfo.side] is set, rules are scoped to the corresponding `@page:left` or `@page:right`
     * selector; otherwise they target `body` and the generic `@page`.
     * @return the CSS string to be embedded in a `<style>` tag
     */
    private fun buildPageFormat(format: PageFormatInfo): String {
        val (selector, isPageSelector) =
            when (format.side) {
                null -> "body" to false
                else -> "@page:${format.side?.asCSS}" to true
            }

        return stylesheet {
            rule(selector) {
                "--qd-content-width" value format.pageWidth
                "--qd-column-count" value format.columnCount?.toString()

                if (format.alignment?.isLocal == true) {
                    "--qd-horizontal-alignment-local" importantValue format.alignment
                    "--qd-horizontal-alignment-global" importantValue "unset"
                    "--qd-horizontal-alignment-list-items" importantValue "unset"
                }
                if (format.alignment?.isGlobal == true) {
                    "--qd-horizontal-alignment-global" importantValue format.alignment
                    "--qd-horizontal-alignment-local" importantValue format.alignment
                }

                format.contentBorderWidth?.let {
                    "--qd-page-content-border-width" value it
                    "--qd-page-content-border-style" value "solid"
                }
                format.contentBorderColor?.let {
                    "--qd-page-content-border-color" value it
                    "--qd-page-content-border-style" value "solid"
                }
            }

            if (!isPageSelector) {
                rule(
                    "body.quarkdown-plain.quarkdown-plain",
                    "body.quarkdown-docs.quarkdown-docs",
                ) {
                    "margin" value format.margin
                }

                rule("body.quarkdown-slides.quarkdown-slides .reveal") {
                    "width" value format.pageWidth
                    "height" value format.pageHeight
                }
            }

            rule(if (isPageSelector) selector else "@page") {
                if (format.pageWidth != null || format.pageHeight != null) {
                    "size" value "${format.pageWidth?.asCSS ?: "auto"} ${format.pageHeight?.asCSS ?: "auto"}"
                }
                "margin" value (format.margin?.asCSS ?: if (document.type == DocumentType.PLAIN) "0" else null)
            }
        }
    }

    /**
     * Extracts font family IDs from the document's font stack using the given [extractor],
     * returning them as a comma-separated CSS value (e.g. `'Roboto', 'Noto Sans'`).
     * Fonts are reversed so that later declarations take higher priority.
     */
    private fun fontFamilyIds(extractor: (FontInfo) -> FontFamily?): String? =
        document.layout.fonts
            .reversed()
            .mapNotNull { extractor(it)?.id }
            .takeIf { it.isNotEmpty() }
            ?.joinToString(", ") { "'$it'" }

    private fun mainFontFamily(): String? = fontFamilyIds { it.mainFamily }

    private fun headingFontFamily(): String? = fontFamilyIds { it.headingFamily }

    private fun codeFontFamily(): String? = fontFamilyIds { it.codeFamily }

    private fun fontSizeCss(): String? =
        document.layout.fonts
            .lastOrNull { it.size != null }
            ?.size
            ?.asCSS

    /** Generates `@font-face` CSS snippets for all font families referenced by the document. */
    private fun fontFaceSnippets(): List<String> {
        val allFamilies: List<FontFamily> =
            document.layout.fonts.flatMap { font ->
                listOfNotNull(font.mainFamily, font.headingFamily, font.codeFamily)
            }
        return CssFontFacesImporter(allFamilies, context.mediaStorage).toSnippets()
    }
}
