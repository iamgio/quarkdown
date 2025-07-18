package com.quarkdown.rendering.html.post

import com.quarkdown.core.ast.attributes.presence.hasCode
import com.quarkdown.core.ast.attributes.presence.hasMath
import com.quarkdown.core.ast.attributes.presence.hasMermaidDiagram
import com.quarkdown.core.context.Context
import com.quarkdown.core.document.DocumentType
import com.quarkdown.core.rendering.template.TemplatePlaceholders
import com.quarkdown.core.template.TemplateProcessor
import com.quarkdown.rendering.html.css.asCSS
import org.apache.commons.text.StringEscapeUtils

/**
 * Supplier of a [TemplateProcessor] that injects content and properties into an HTML template.
 * @see HtmlPostRenderer
 */
class HtmlPostRendererTemplate(
    private val base: TemplateProcessor,
    private val context: Context,
) {
    private val document = context.documentInfo

    /**
     * @return a copy of the base [TemplateProcessor] with all necessary content and properties injected.
     * @see TemplatePlaceholders
     */
    fun create(): TemplateProcessor =
        base.copy().apply {
            serverPort()
            documentMetadata()
            thirdParty()
            pageFormat()
            paragraphStyling()
            fonts()
            texMacros()
        }

    /**
     * Document metadata and type.
     * @see com.quarkdown.core.document.DocumentInfo
     */
    private fun TemplateProcessor.documentMetadata() {
        value(TemplatePlaceholders.TITLE, document.name ?: "Quarkdown")
        optionalValue(TemplatePlaceholders.LANGUAGE, document.locale?.tag)
        value(
            TemplatePlaceholders.DOCUMENT_TYPE,
            document.type.name.lowercase(),
        )
        // "Plain" document rendering.
        conditional(TemplatePlaceholders.IS_PLAIN, document.type == DocumentType.PLAIN)
        // "Paged" document rendering via PagesJS.
        conditional(TemplatePlaceholders.IS_PAGED, document.type == DocumentType.PAGED)
        // "Slides" document rendering via RevealJS.
        conditional(TemplatePlaceholders.IS_SLIDES, document.type == DocumentType.SLIDES)
    }

    /**
     * Initialization of third-party libraries only if needed.
     * This includes HighlightJS, Mermaid, and KaTeX.
     */
    private fun TemplateProcessor.thirdParty() {
        // HighlightJS for code blocks.
        conditional(
            TemplatePlaceholders.HAS_CODE,
            context.attributes.hasCode,
        )
        // Mermaid for diagrams.
        conditional(
            TemplatePlaceholders.HAS_MERMAID_DIAGRAM,
            context.attributes.hasMermaidDiagram,
        )
        // KaTeX for math rendering.
        conditional(
            TemplatePlaceholders.HAS_MATH,
            context.attributes.hasMath,
        )
    }

    /**
     * Page format and layout information.
     * @see com.quarkdown.core.document.layout.page.PageFormatInfo
     */
    private fun TemplateProcessor.pageFormat() {
        val pageFormat = document.layout.pageFormat

        conditional(TemplatePlaceholders.HAS_PAGE_SIZE, pageFormat.hasSize)
        optionalValue(
            TemplatePlaceholders.PAGE_WIDTH,
            pageFormat.pageWidth?.asCSS,
        )
        optionalValue(
            TemplatePlaceholders.PAGE_HEIGHT,
            pageFormat.pageHeight?.asCSS,
        )
        optionalValue(
            TemplatePlaceholders.PAGE_MARGIN,
            pageFormat.margin?.asCSS,
        )
        optionalValue(
            TemplatePlaceholders.PAGE_CONTENT_BORDER_WIDTH,
            pageFormat.contentBorderWidth?.asCSS,
        )
        optionalValue(
            TemplatePlaceholders.PAGE_CONTENT_BORDER_COLOR,
            pageFormat.contentBorderColor?.asCSS,
        )
        optionalValue(
            TemplatePlaceholders.COLUMN_COUNT,
            pageFormat.columnCount,
        )
        // Alignment can be global or local. See TemplatePlaceholders.GLOBAL_HORIZONTAL_ALIGNMENT for details.
        optionalValue(
            TemplatePlaceholders.GLOBAL_HORIZONTAL_ALIGNMENT,
            pageFormat.alignment?.takeIf { it.isGlobal }?.asCSS,
        )
        optionalValue(
            TemplatePlaceholders.LOCAL_HORIZONTAL_ALIGNMENT,
            pageFormat.alignment?.takeIf { it.isLocal }?.asCSS,
        )
    }

    /**
     * Paragraph styling information.
     * @see com.quarkdown.core.document.layout.paragraph.ParagraphStyleInfo
     */
    private fun TemplateProcessor.paragraphStyling() {
        val paragraphStyle = document.layout.paragraphStyle

        optionalValue(
            TemplatePlaceholders.PARAGRAPH_SPACING,
            paragraphStyle.spacing
                ?.toString()
                ?.plus("em"),
        )
        optionalValue(
            TemplatePlaceholders.PARAGRAPH_LINE_HEIGHT,
            paragraphStyle.lineHeight,
        )
        optionalValue(
            TemplatePlaceholders.PARAGRAPH_LETTER_SPACING,
            paragraphStyle.letterSpacing
                ?.toString()
                ?.plus("em"),
        )
        optionalValue(
            TemplatePlaceholders.PARAGRAPH_INDENT,
            paragraphStyle.indent
                ?.toString()
                ?.plus("em"),
        )
    }

    /**
     * Font configuration.
     * @see com.quarkdown.core.document.layout.font.FontInfo
     */
    private fun TemplateProcessor.fonts() {
        val fontConfiguration = document.layout.font
        val fontFamilies =
            mapOf(
                TemplatePlaceholders.MAIN_FONT_FAMILY to fontConfiguration.mainFamily,
                TemplatePlaceholders.HEADING_FONT_FAMILY to fontConfiguration.headingFamily,
                TemplatePlaceholders.CODE_FONT_FAMILY to fontConfiguration.codeFamily,
            )

        // Font families.
        fontFamilies.forEach { (placeholder, fontFamily) -> optionalValue(placeholder, fontFamily?.id) }
        // Font size.
        optionalValue(
            TemplatePlaceholders.FONT_SIZE,
            fontConfiguration.size?.asCSS,
        )
        // Imports fonts via @font-face or @import rules.
        iterable(
            TemplatePlaceholders.FONT_FACES,
            CssFontFacesImporter(fontFamilies.values.filterNotNull(), context.mediaStorage)
                .toSnippets(),
        )
    }

    private fun sanitizeJs(text: String): String = StringEscapeUtils.escapeEcmaScript(text)

    private fun mapToJsObjectEntries(map: Map<String, String>): List<String> =
        map.map { (key, value) ->
            "\"${sanitizeJs(key)}\": \"${sanitizeJs(value)}\""
        }

    private fun TemplateProcessor.texMacros() {
        iterable(
            TemplatePlaceholders.TEX_MACROS,
            mapToJsObjectEntries(context.documentInfo.tex.macros),
        )
    }

    /**
     * Local server port to communicate with.
     */
    private fun TemplateProcessor.serverPort() {
        optionalValue(
            TemplatePlaceholders.SERVER_PORT,
            context.attachedPipeline
                ?.options
                ?.serverPort,
        )
    }
}
