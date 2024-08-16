package eu.iamgio.quarkdown.rendering.html

import eu.iamgio.quarkdown.ast.AstRoot
import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.ast.base.block.BaseListItem
import eu.iamgio.quarkdown.ast.base.block.Heading
import eu.iamgio.quarkdown.ast.base.block.OrderedList
import eu.iamgio.quarkdown.ast.base.inline.CodeSpan
import eu.iamgio.quarkdown.ast.base.inline.Image
import eu.iamgio.quarkdown.ast.base.inline.Text
import eu.iamgio.quarkdown.ast.id.getId
import eu.iamgio.quarkdown.ast.quarkdown.FunctionCallNode
import eu.iamgio.quarkdown.ast.quarkdown.block.Aligned
import eu.iamgio.quarkdown.ast.quarkdown.block.Box
import eu.iamgio.quarkdown.ast.quarkdown.block.Clipped
import eu.iamgio.quarkdown.ast.quarkdown.block.Math
import eu.iamgio.quarkdown.ast.quarkdown.block.PageBreak
import eu.iamgio.quarkdown.ast.quarkdown.block.SlidesFragment
import eu.iamgio.quarkdown.ast.quarkdown.block.Stacked
import eu.iamgio.quarkdown.ast.quarkdown.block.TableOfContents
import eu.iamgio.quarkdown.ast.quarkdown.inline.MathSpan
import eu.iamgio.quarkdown.ast.quarkdown.inline.TextTransform
import eu.iamgio.quarkdown.ast.quarkdown.inline.Whitespace
import eu.iamgio.quarkdown.ast.quarkdown.invisible.PageCounterInitializer
import eu.iamgio.quarkdown.ast.quarkdown.invisible.PageMarginContentInitializer
import eu.iamgio.quarkdown.ast.quarkdown.invisible.SlidesConfigurationInitializer
import eu.iamgio.quarkdown.context.Context
import eu.iamgio.quarkdown.context.shouldAutoPageBreak
import eu.iamgio.quarkdown.document.DocumentType
import eu.iamgio.quarkdown.rendering.tag.buildMultiTag
import eu.iamgio.quarkdown.rendering.tag.buildTag
import eu.iamgio.quarkdown.rendering.tag.tagBuilder
import eu.iamgio.quarkdown.util.toPlainText

private const val BLOCK_MATH_FENCE = "__QD_BLOCK_MATH__"
private const val INLINE_MATH_FENCE = "__QD_INLINE_MATH__"

/**
 * A renderer for Quarkdown ([eu.iamgio.quarkdown.flavor.quarkdown.QuarkdownFlavor]) nodes that exports their content into valid HTML code.
 * @param context additional information produced by the earlier stages of the pipeline
 */
class QuarkdownHtmlNodeRenderer(context: Context) : BaseHtmlNodeRenderer(context) {
    /**
     * A `<div class="styleClass">...</div>` tag.
     */
    private fun div(
        styleClass: String? = null,
        init: HtmlTagBuilder.() -> Unit,
    ) = tagBuilder("div", init = init)
        .`class`(styleClass)
        .build()

    /**
     * A `<div class="styleClass">children</div>` tag.
     */
    private fun div(
        styleClass: String,
        children: List<Node>,
    ) = div(styleClass) { +children }

    // Quarkdown node rendering

    // The function was already expanded by previous stages: its output nodes are stored in its children.
    override fun visit(node: FunctionCallNode): CharSequence = visit(AstRoot(node.children))

    // Block

    // An empty div that acts as a page break.
    override fun visit(node: PageBreak) = div("page-break") {}

    // Math is processed by the MathJax library which requires text delimiters instead of tags.
    override fun visit(node: Math) = BLOCK_MATH_FENCE + "$" + node.expression + "$" + BLOCK_MATH_FENCE

    override fun visit(node: Aligned) = div("align align-" + node.alignment.name.lowercase(), node.children)

    override fun visit(node: Stacked) =
        div("stack stack-${node.layout.asCSS}") {
            +node.children

            style {
                if (node.layout is Stacked.Grid) {
                    // The amount of 'auto' matches the amount of columns/rows.
                    "grid-template-columns" value "auto ".repeat(node.layout.columnCount).trimEnd()
                }

                "justify-content" value node.mainAxisAlignment
                "align-items" value node.crossAxisAlignment
                "gap" value node.gap
            }
        }

    override fun visit(node: Clipped) = div("clip clip-${node.clip.asCSS}", node.children)

    override fun visit(node: Box) =
        div {
            classes("box", node.type.asCSS)

            if (node.title != null) {
                tag("header") {
                    tag("h4", node.title)

                    style {
                        "color" value node.foregroundColor // Must be repeated to force override.
                        "padding" value node.padding
                    }
                }
            }

            // Box actual content.
            +div("box-content") {
                +node.children

                style { "padding" value node.padding }
            }

            // Box style. Padding is applied separately to the header and the content.
            style {
                "background-color" value node.backgroundColor
                "color" value node.foregroundColor
            }
        }

    override fun visit(node: Whitespace) =
        buildTag("span") {
            style {
                "width" value node.width
                "height" value node.height
            }
        }

    // A table of contents is rendered as an ordered list.
    private fun tableOfContentsItemsToList(items: List<TableOfContents.Item>) =
        OrderedList(
            startIndex = 1,
            isLoose = true,
            children = items.map { BaseListItem(listOf(it)) },
        )

    override fun visit(node: TableOfContents) =
        buildMultiTag {
            // Title
            +Heading(1, listOf(Text("Table of Contents")), customId = "table-of-contents") // TODO localize
            // Content
            +div("table-of-contents") {
                +tableOfContentsItemsToList(node.items)
            }
        }

    override fun visit(node: TableOfContents.Item): CharSequence {
        val link =
            buildTag("a") {
                +node.text

                // Link to the target anchor (e.g. a heading).
                attribute("href", "#" + HtmlIdentifierProvider.of(this@QuarkdownHtmlNodeRenderer).getId(node.target))
            }

        return buildMultiTag {
            +link

            // Recursively render sub-items.
            if (node.subItems.isNotEmpty()) {
                +tableOfContentsItemsToList(node.subItems)
            }
        }
    }

    // Inline

    // Math is processed by the MathJax library which requires text delimiters instead of tags.
    override fun visit(node: MathSpan) = INLINE_MATH_FENCE + "$" + node.expression + "$" + INLINE_MATH_FENCE

    override fun visit(node: SlidesFragment): CharSequence =
        tagBuilder("div", node.children)
            .classes("fragment", node.behavior.asCSS)
            .build()

    override fun visit(node: TextTransform) =
        buildTag("span") {
            +node.children

            `class`(node.data.size?.asCSS) // e.g. 'size-small' class

            style {
                "font-weight" value node.data.weight
                "font-style" value node.data.style
                "font-variant" value node.data.variant
                "text-decoration" value node.data.decoration
                "text-transform" value node.data.case
                "color" value node.data.color
            }
        }

    // Invisible nodes

    override fun visit(node: PageMarginContentInitializer) =
        if (context.documentInfo.type == DocumentType.PAGED) {
            // Paged documents support only plain text content.
            buildTag("script") {
                // Inject a CSS property used by the HTML wrapper.
                val property = "--page-margin-${node.position.asCSS}-content"
                val content = node.children.toPlainText(renderer = this@QuarkdownHtmlNodeRenderer)
                +"document.documentElement.style.setProperty('$property', '\"$content\"');"
            }
        } else {
            // HTML content.
            // In slides, these elements are copied to each slide through the slides.js script.
            div("page-margin-content page-margin-${node.position.asCSS}", node.children)
        }

    override fun visit(node: PageCounterInitializer) =
        visit(
            PageMarginContentInitializer(
                children =
                    when (context.documentInfo.type) {
                        DocumentType.PAGED ->
                            // Handled by PagedJS CSS content property.
                            node.content(
                                "\"counter(page)\"",
                                "\"counter(pages)\"",
                            )

                        DocumentType.SLIDES ->
                            node.content(
                                // Get the current slide index.
                                tagBuilder("span")
                                    .`class`("current-page-number")
                                    .build(),
                                // Get the total amount of slides.
                                tagBuilder("span")
                                    .`class`("total-page-number")
                                    .build(),
                            )

                        else -> node.content("-", "-") // Placeholder for document types that don't support page counters.
                    },
                position = node.position,
            ),
        )

    override fun visit(node: SlidesConfigurationInitializer): CharSequence =
        buildTag("script") {
            // Inject properties that are read by the slides.js script after the document is loaded.
            +buildString {
                node.centerVertically?.let {
                    append("const slides_center = $it;")
                }
                node.showControls?.let {
                    append("const slides_showControls = $it;")
                }
                node.transition?.let {
                    append("const slides_transitionStyle = '${it.style.asCSS}';")
                    append("const slides_transitionSpeed = '${it.speed.asCSS}';")
                }
            }
        }

    // Additional behavior of base nodes

    // On top of the default behavior, an anchor ID is set,
    // and it could force an automatic page break if suitable.
    override fun visit(node: Heading): String {
        val headingTag =
            tagBuilder("h${node.depth}", node.text)
                .optionalAttribute(
                    "id",
                    // Generate an automatic identifier if allowed by settings.
                    HtmlIdentifierProvider.of(renderer = this)
                        .takeIf { context.options.enableAutomaticIdentifiers || node.customId != null }
                        ?.getId(node),
                )
                .build()

        return buildMultiTag {
            if (context.shouldAutoPageBreak(node)) {
                +PageBreak()
            }
            +headingTag
        }
    }

    // The Quarkdown flavor renders an image title as a figure caption, if present.
    override fun visit(node: Image): String {
        val imgTag = super.visit(node)

        return node.link.title?.let { title ->
            buildTag("figure") {
                +imgTag
                +buildTag("figcaption", title)
            }
        } ?: imgTag
    }

    // A code span can contain additional content, such as a color preview.
    override fun visit(node: CodeSpan): String {
        val codeTag = super.visit(node)

        // The code is wrapped to allow additional content.
        return buildTag("span") {
            `class`("codespan-content")

            +codeTag

            when (node.content) {
                null -> {} // No additional content.
                is CodeSpan.ColorContent -> {
                    // If the code contains a color code, show the color preview.
                    +buildTag("span") {
                        style { "background-color" value node.content.color }
                        `class`("color-preview")
                    }
                }
            }
        }
    }
}
