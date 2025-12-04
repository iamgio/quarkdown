package com.quarkdown.rendering.html.node

import com.quarkdown.core.ast.AstRoot
import com.quarkdown.core.ast.attributes.id.getId
import com.quarkdown.core.ast.attributes.link.getResolvedUrl
import com.quarkdown.core.ast.attributes.reference.getDefinition
import com.quarkdown.core.ast.base.block.BlankNode
import com.quarkdown.core.ast.base.block.BlockQuote
import com.quarkdown.core.ast.base.block.Code
import com.quarkdown.core.ast.base.block.FootnoteDefinition
import com.quarkdown.core.ast.base.block.Heading
import com.quarkdown.core.ast.base.block.HorizontalRule
import com.quarkdown.core.ast.base.block.Html
import com.quarkdown.core.ast.base.block.LinkDefinition
import com.quarkdown.core.ast.base.block.Newline
import com.quarkdown.core.ast.base.block.Paragraph
import com.quarkdown.core.ast.base.block.Table
import com.quarkdown.core.ast.base.block.getFormattedIndex
import com.quarkdown.core.ast.base.block.getIndex
import com.quarkdown.core.ast.base.block.list.ListItem
import com.quarkdown.core.ast.base.block.list.ListItemVariantVisitor
import com.quarkdown.core.ast.base.block.list.OrderedList
import com.quarkdown.core.ast.base.block.list.TaskListItemVariant
import com.quarkdown.core.ast.base.block.list.UnorderedList
import com.quarkdown.core.ast.base.inline.CheckBox
import com.quarkdown.core.ast.base.inline.CodeSpan
import com.quarkdown.core.ast.base.inline.Comment
import com.quarkdown.core.ast.base.inline.Emphasis
import com.quarkdown.core.ast.base.inline.Image
import com.quarkdown.core.ast.base.inline.LineBreak
import com.quarkdown.core.ast.base.inline.Link
import com.quarkdown.core.ast.base.inline.ReferenceFootnote
import com.quarkdown.core.ast.base.inline.ReferenceImage
import com.quarkdown.core.ast.base.inline.ReferenceLink
import com.quarkdown.core.ast.base.inline.Strikethrough
import com.quarkdown.core.ast.base.inline.Strong
import com.quarkdown.core.ast.base.inline.StrongEmphasis
import com.quarkdown.core.ast.base.inline.SubdocumentLink
import com.quarkdown.core.ast.base.inline.Text
import com.quarkdown.core.ast.base.inline.getSubdocument
import com.quarkdown.core.ast.media.getStoredMedia
import com.quarkdown.core.ast.quarkdown.FunctionCallNode
import com.quarkdown.core.ast.quarkdown.bibliography.BibliographyCitation
import com.quarkdown.core.ast.quarkdown.bibliography.BibliographyView
import com.quarkdown.core.ast.quarkdown.block.Box
import com.quarkdown.core.ast.quarkdown.block.Clipped
import com.quarkdown.core.ast.quarkdown.block.Collapse
import com.quarkdown.core.ast.quarkdown.block.Container
import com.quarkdown.core.ast.quarkdown.block.Figure
import com.quarkdown.core.ast.quarkdown.block.FullColumnSpan
import com.quarkdown.core.ast.quarkdown.block.Landscape
import com.quarkdown.core.ast.quarkdown.block.Math
import com.quarkdown.core.ast.quarkdown.block.MermaidDiagram
import com.quarkdown.core.ast.quarkdown.block.Numbered
import com.quarkdown.core.ast.quarkdown.block.PageBreak
import com.quarkdown.core.ast.quarkdown.block.SlidesFragment
import com.quarkdown.core.ast.quarkdown.block.SlidesSpeakerNote
import com.quarkdown.core.ast.quarkdown.block.Stacked
import com.quarkdown.core.ast.quarkdown.block.SubdocumentGraph
import com.quarkdown.core.ast.quarkdown.block.list.FocusListItemVariant
import com.quarkdown.core.ast.quarkdown.block.list.LocationTargetListItemVariant
import com.quarkdown.core.ast.quarkdown.block.toc.TableOfContentsView
import com.quarkdown.core.ast.quarkdown.inline.InlineCollapse
import com.quarkdown.core.ast.quarkdown.inline.LastHeading
import com.quarkdown.core.ast.quarkdown.inline.MathSpan
import com.quarkdown.core.ast.quarkdown.inline.PageCounter
import com.quarkdown.core.ast.quarkdown.inline.TextSymbol
import com.quarkdown.core.ast.quarkdown.inline.TextTransform
import com.quarkdown.core.ast.quarkdown.inline.Whitespace
import com.quarkdown.core.ast.quarkdown.invisible.PageMarginContentInitializer
import com.quarkdown.core.ast.quarkdown.invisible.SlidesConfigurationInitializer
import com.quarkdown.core.ast.quarkdown.reference.CrossReference
import com.quarkdown.core.context.Context
import com.quarkdown.core.context.resolveOrFallback
import com.quarkdown.core.document.sub.Subdocument
import com.quarkdown.core.document.sub.getOutputFileName
import com.quarkdown.core.rendering.UnsupportedRenderException
import com.quarkdown.core.rendering.tag.TagNodeRenderer
import com.quarkdown.core.rendering.tag.buildMultiTag
import com.quarkdown.core.rendering.tag.buildTag
import com.quarkdown.core.rendering.tag.tagBuilder
import com.quarkdown.core.util.Escape
import com.quarkdown.core.util.toPlainText
import com.quarkdown.rendering.html.HtmlIdentifierProvider
import com.quarkdown.rendering.html.HtmlTagBuilder
import com.quarkdown.rendering.html.css.asCSS

/**
 * A renderer for vanilla Markdown ([com.quarkdown.core.flavor.base.BaseMarkdownFlavor]) nodes that exports their content into valid HTML code.
 * @param context additional information produced by the earlier stages of the pipeline
 */
open class BaseHtmlNodeRenderer(
    context: Context,
) : TagNodeRenderer<HtmlTagBuilder>(context),
    // Along with nodes, this component is also responsible for rendering list item variants.
    // For instance, a checked/unchecked task of attached to a list item.
    // These flavors directly affect the behavior of the HTML list item builder.
    ListItemVariantVisitor<HtmlTagBuilder.() -> Unit> {
    override fun createBuilder(
        name: String,
        pretty: Boolean,
    ) = HtmlTagBuilder(name, renderer = this, pretty)

    override fun escapeCriticalContent(unescaped: String) =
        unescaped
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("\'", "&#39;")

    // Root

    override fun visit(node: AstRoot) =
        buildMultiTag {
            +node.children
        }

    // Block

    override fun visit(node: Newline) = ""

    override fun visit(node: Code) =
        buildTag("pre") {
            tag("code") {
                +escapeCriticalContent(node.content)

                classNames(
                    // Sets the code language.
                    node.language?.let { "language-$it" },
                    // Disables syntax highlighting.
                    "no-highlight".takeUnless { node.highlight },
                    // Disables line numbers.
                    "nohljsln".takeUnless { node.showLineNumbers },
                    // Focuses certain lines.
                    "focus-lines".takeIf { node.focusedLines != null },
                )

                // Focus range.
                optionalAttribute("data-focus-start", node.focusedLines?.start)
                optionalAttribute("data-focus-end", node.focusedLines?.end)
            }
        }

    override fun visit(node: HorizontalRule) =
        tagBuilder("hr")
            .void(true)
            .build()

    override fun visit(node: Heading) = buildTag("h${node.depth}", node.text)

    override fun visit(node: LinkDefinition) = "" // Not rendered

    override fun visit(node: FootnoteDefinition): CharSequence {
        val index = node.getIndex(context) ?: return "" // The footnote is rendered only if it is linked to a reference
        val formattedIndex = node.getFormattedIndex(context) ?: return ""

        return buildTag("span") {
            className("footnote-definition")
            optionalAttribute("id", HtmlIdentifierProvider.of(this@BaseHtmlNodeRenderer).getId(node))
            optionalAttribute("data-footnote-index", index)

            tag("sup") {
                className("footnote-label")
                +formattedIndex
            }
            tag("span") {
                +node.text
            }
        }
    }

    override fun visit(node: OrderedList) =
        tagBuilder("ol", node.children)
            .optionalAttribute("start", node.startIndex.takeUnless { it == 1 })
            .build()

    override fun visit(node: UnorderedList) = buildTag("ul", node.children)

    // Appends the base content of a list item, following the loose/tight rendering rules (CommonMark 5.3).
    override fun visit(node: ListItem) =
        buildTag("li") {
            // Flavors are executed on this HTML builder.
            node.variants.forEach { it.accept(this@BaseHtmlNodeRenderer).invoke(this) }

            // Loose lists (or items not linked to a list for some reason) are rendered as-is.
            if (node.owner?.isLoose != false) {
                // This base builder is empty by default.
                // If any of the variants added some content (e.g. a task checkbox),
                // the actual content is wrapped in a container for more convenient styling.
                when {
                    this.isEmpty -> +node.children
                    else -> +buildTag("div", node.children)
                }
                return@buildTag
            }
            // Tight lists don't wrap paragraphs in <p> tags (CommonMark 5.3).
            node.children.forEach {
                when (it) {
                    is Paragraph -> +it.text
                    else -> +it
                }
            }
        }

    // GFM 5.3 extension.
    override fun visit(variant: TaskListItemVariant): HtmlTagBuilder.() -> Unit =
        {
            className("task-list-item")
            +visit(CheckBox(variant.isChecked))
        }

    override fun visit(node: Html) = node.content

    /**
     * Table tag builder, enhanceable by subclasses.
     */
    protected fun tableBuilder(node: Table): HtmlTagBuilder =
        tagBuilder("table") {
            // Tables are stored by columns and here transposed to a row-based structure.
            val header = tag("thead")
            val headerRow = header.tag("tr")
            val body = tag("tbody")
            val bodyRows = mutableListOf<HtmlTagBuilder>()

            node.columns.forEach { column ->
                // Value to assign to the 'align' attribute for each cell of this column.
                val alignment = column.alignment.takeUnless { it == Table.Alignment.NONE }?.asCSS

                // Header cell.
                headerRow
                    .tag("th", column.header.text)
                    .optionalAttribute("align", alignment)

                // Body cells.
                column.cells.forEachIndexed { index, cell ->
                    // Adding a new row if needed.
                    if (index >= bodyRows.size) {
                        bodyRows += body.tag("tr")
                    }
                    // Adding a cell.
                    bodyRows[index]
                        .tag("td", cell.text)
                        .optionalAttribute("align", alignment)
                }
            }
        }

    override fun visit(node: Table) = tableBuilder(node).build()

    override fun visit(node: Paragraph) = buildTag("p", node.text)

    override fun visit(node: BlockQuote) = buildTag("blockquote", node.children)

    override fun visit(node: BlankNode) = "" // Fallback block, should not happen

    // Inline

    override fun visit(node: Comment) = "" // Ignored

    override fun visit(node: LineBreak) =
        tagBuilder("br")
            .void(true)
            .build()

    override fun visit(node: Link) =
        tagBuilder("a", node.label)
            .attribute("href", node.url)
            .optionalAttribute("title", node.title)
            .build()

    // The fallback node is rendered if a corresponding definition can't be found.
    override fun visit(node: ReferenceLink) = context.resolveOrFallback(node).accept(this)

    override fun visit(node: SubdocumentLink): CharSequence {
        val subdocument: Subdocument =
            node.getSubdocument(context)
                ?: return "[???]"

        return Link(
            label = node.label,
            url = "./${subdocument.getOutputFileName(context)}.html",
            title = node.title,
        ).accept(this)
    }

    override fun visit(node: ReferenceFootnote): CharSequence {
        val definition: FootnoteDefinition =
            node.getDefinition(context)
                ?: return node.fallback().accept(this)

        return buildTag("sup") {
            classNames("footnote-reference", "footnote-label")
            val definitionId = HtmlIdentifierProvider.of(this@BaseHtmlNodeRenderer).getId(definition)
            attribute("data-definition", definitionId)
            tag("a") {
                optionalAttribute("href", "#$definitionId")
                +(definition.getFormattedIndex(context) ?: "?")
            }
        }
    }

    override fun visit(node: Image) =
        tagBuilder("img")
            .attribute("src", node.link.getStoredMedia(context)?.path ?: node.link.getResolvedUrl(context))
            .attribute("alt", node.link.label.toPlainText(renderer = this)) // Emphasis is discarded (CommonMark 6.4)
            .optionalAttribute("title", node.link.title)
            .style {
                "width" value node.width
                "height" value node.height
            }.void(true)
            .build()

    override fun visit(node: ReferenceImage) = context.resolveOrFallback(node).accept(this)

    override fun visit(node: CheckBox) =
        tagBuilder("input") {}
            .attribute("disabled", "")
            .attribute("type", "checkbox")
            .optionalAttribute("checked", "".takeIf { node.isChecked })
            .void(true)
            .build()

    override fun visit(node: Text) = node.text

    override fun visit(node: TextSymbol) = Escape.Html.escape(node.text) // e.g. © -> &copy;

    override fun visit(node: CodeSpan) = buildTag("code", escapeCriticalContent(node.text))

    override fun visit(node: Emphasis) = buildTag("em", node.children)

    override fun visit(node: Strong) = buildTag("strong", node.children)

    override fun visit(node: StrongEmphasis) =
        buildTag("em") {
            tag("strong") {
                +node.children
            }
        }

    override fun visit(node: Strikethrough) = buildTag("del", node.children)

    // Quarkdown - implemented by QuarkdownHtmlNodeRenderer

    override fun visit(node: FunctionCallNode): CharSequence = throw UnsupportedRenderException(node)

    override fun visit(node: Figure<*>): CharSequence = throw UnsupportedRenderException(node)

    override fun visit(node: PageBreak): CharSequence = throw UnsupportedRenderException(node)

    override fun visit(node: Math): CharSequence = throw UnsupportedRenderException(node)

    override fun visit(node: Container): CharSequence = throw UnsupportedRenderException(node)

    override fun visit(node: Stacked): CharSequence = throw UnsupportedRenderException(node)

    override fun visit(node: Numbered): CharSequence = throw UnsupportedRenderException(node)

    override fun visit(node: Landscape): CharSequence = throw UnsupportedRenderException(node)

    override fun visit(node: FullColumnSpan): CharSequence = throw UnsupportedRenderException(node)

    override fun visit(node: Clipped): CharSequence = throw UnsupportedRenderException(node)

    override fun visit(node: Box): CharSequence = throw UnsupportedRenderException(node)

    override fun visit(node: Collapse): CharSequence = throw UnsupportedRenderException(node)

    override fun visit(node: Whitespace): CharSequence = throw UnsupportedRenderException(node)

    override fun visit(node: TableOfContentsView): CharSequence = throw UnsupportedRenderException(node)

    override fun visit(node: BibliographyView): CharSequence = throw UnsupportedRenderException(node)

    override fun visit(node: MermaidDiagram): CharSequence = throw UnsupportedRenderException(node)

    override fun visit(node: SubdocumentGraph): CharSequence = throw UnsupportedRenderException(node)

    override fun visit(node: PageMarginContentInitializer): CharSequence = throw UnsupportedRenderException(node)

    override fun visit(node: PageCounter): CharSequence = throw UnsupportedRenderException(node)

    override fun visit(node: LastHeading): CharSequence = throw UnsupportedRenderException(node)

    override fun visit(node: SlidesConfigurationInitializer): CharSequence = throw UnsupportedRenderException(node)

    override fun visit(node: MathSpan): CharSequence = throw UnsupportedRenderException(node)

    override fun visit(node: TextTransform): CharSequence = throw UnsupportedRenderException(node)

    override fun visit(node: InlineCollapse): CharSequence = throw UnsupportedRenderException(node)

    override fun visit(node: CrossReference): CharSequence = throw UnsupportedRenderException(node)

    override fun visit(node: BibliographyCitation): CharSequence = throw UnsupportedRenderException(node)

    override fun visit(node: SlidesFragment): CharSequence = throw UnsupportedRenderException(node)

    override fun visit(node: SlidesSpeakerNote): CharSequence = throw UnsupportedRenderException(node)

    override fun visit(variant: FocusListItemVariant): HtmlTagBuilder.() -> Unit = throw UnsupportedRenderException(variant::class)

    override fun visit(variant: LocationTargetListItemVariant): HtmlTagBuilder.() -> Unit = throw UnsupportedRenderException(variant::class)
}
