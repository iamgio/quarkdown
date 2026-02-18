package com.quarkdown.rendering.html.post.document

import com.quarkdown.core.context.Context
import com.quarkdown.core.document.DocumentType
import com.quarkdown.core.document.layout.font.FontInfo
import com.quarkdown.core.misc.font.FontFamily
import com.quarkdown.core.util.get
import com.quarkdown.core.util.withDefault
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
) {
    private val document = context.documentInfo

    private val pageFormat = document.layout.pageFormat.withDefault(document.type.defaultPageFormat)

    /**
     * Builds the full CSS stylesheet content for the document.
     * @return the CSS string to be embedded in a `<style>` tag
     */
    fun build(): String {
        val pageWidth = pageFormat.get { pageWidth }
        val pageHeight = pageFormat.get { pageHeight }
        val margin = pageFormat.get { margin }
        val alignment = pageFormat.get { alignment }
        val paragraphStyle = document.layout.paragraphStyle

        return stylesheet {
            raw(fontFaceSnippets().joinToString("\n"))

            rule("body") {
                "--qd-main-custom-font" value mainFontFamily()
                "--qd-heading-custom-font" value headingFontFamily()
                "--qd-code-custom-font" value codeFontFamily()
                "--qd-main-font-size" value fontSizeCss()
                "--qd-content-width" value pageWidth
                "--qd-column-count" value pageFormat.get { columnCount }?.toString()

                if (alignment != null && alignment.isLocal) {
                    "--qd-horizontal-alignment-local" importantValue alignment
                    "--qd-horizontal-alignment-global" importantValue "unset"
                    "--qd-horizontal-alignment-list-items" importantValue "unset"
                }
                if (alignment != null && alignment.isGlobal) {
                    "--qd-horizontal-alignment-global" importantValue alignment
                    "--qd-horizontal-alignment-local" importantValue alignment
                }

                pageFormat.get { contentBorderWidth }?.let {
                    "--qd-page-content-border-width" value it
                    "--qd-page-content-border-style" value "solid"
                }
                pageFormat.get { contentBorderColor }?.let {
                    "--qd-page-content-border-color" value it
                    "--qd-page-content-border-style" value "solid"
                }

                "--qd-line-height" value paragraphStyle.lineHeight?.toString()
                "--qd-letter-spacing" value paragraphStyle.letterSpacing?.let { "${it}em" }
                "--qd-paragraph-vertical-margin" value paragraphStyle.spacing?.let { "${it}em" }
            }

            rule(
                "body.quarkdown-plain.quarkdown-plain",
                "body.quarkdown-docs.quarkdown-docs",
            ) {
                "margin" value margin
            }

            rule("body.quarkdown-slides.quarkdown-slides .reveal") {
                "width" value pageWidth
                "height" value pageHeight
            }

            rule("@page") {
                "size" value "${pageWidth?.asCSS ?: "auto"} ${pageHeight?.asCSS ?: "auto"}"
                "margin" value (margin?.asCSS ?: if (document.type == DocumentType.PLAIN) "0" else null)
            }

            rule("p") {
                "--qd-paragraph-text-indent" value paragraphStyle.indent?.let { "${it}em" }
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
