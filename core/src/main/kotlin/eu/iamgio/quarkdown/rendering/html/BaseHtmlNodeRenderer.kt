package eu.iamgio.quarkdown.rendering.html

import eu.iamgio.quarkdown.ast.AstRoot
import eu.iamgio.quarkdown.ast.BaseListItem
import eu.iamgio.quarkdown.ast.BlockQuote
import eu.iamgio.quarkdown.ast.BlockText
import eu.iamgio.quarkdown.ast.CheckBox
import eu.iamgio.quarkdown.ast.Code
import eu.iamgio.quarkdown.ast.CodeSpan
import eu.iamgio.quarkdown.ast.Comment
import eu.iamgio.quarkdown.ast.Emphasis
import eu.iamgio.quarkdown.ast.FunctionCallNode
import eu.iamgio.quarkdown.ast.Heading
import eu.iamgio.quarkdown.ast.HorizontalRule
import eu.iamgio.quarkdown.ast.Html
import eu.iamgio.quarkdown.ast.Image
import eu.iamgio.quarkdown.ast.LineBreak
import eu.iamgio.quarkdown.ast.Link
import eu.iamgio.quarkdown.ast.LinkDefinition
import eu.iamgio.quarkdown.ast.ListItem
import eu.iamgio.quarkdown.ast.Math
import eu.iamgio.quarkdown.ast.MathSpan
import eu.iamgio.quarkdown.ast.Newline
import eu.iamgio.quarkdown.ast.OrderedList
import eu.iamgio.quarkdown.ast.PageBreak
import eu.iamgio.quarkdown.ast.Paragraph
import eu.iamgio.quarkdown.ast.ReferenceImage
import eu.iamgio.quarkdown.ast.ReferenceLink
import eu.iamgio.quarkdown.ast.Strikethrough
import eu.iamgio.quarkdown.ast.Strong
import eu.iamgio.quarkdown.ast.StrongEmphasis
import eu.iamgio.quarkdown.ast.Table
import eu.iamgio.quarkdown.ast.TaskListItem
import eu.iamgio.quarkdown.ast.Text
import eu.iamgio.quarkdown.ast.UnorderedList
import eu.iamgio.quarkdown.ast.quarkdown.Aligned
import eu.iamgio.quarkdown.ast.quarkdown.Box
import eu.iamgio.quarkdown.ast.quarkdown.Clipped
import eu.iamgio.quarkdown.ast.quarkdown.PageCounterInitializer
import eu.iamgio.quarkdown.ast.quarkdown.PageMarginContentInitializer
import eu.iamgio.quarkdown.ast.quarkdown.SlidesConfigurationInitializer
import eu.iamgio.quarkdown.ast.quarkdown.Stacked
import eu.iamgio.quarkdown.ast.quarkdown.TextTransform
import eu.iamgio.quarkdown.context.Context
import eu.iamgio.quarkdown.context.resolveOrFallback
import eu.iamgio.quarkdown.rendering.UnsupportedRenderException
import eu.iamgio.quarkdown.rendering.tag.TagNodeRenderer
import eu.iamgio.quarkdown.rendering.tag.buildTag
import eu.iamgio.quarkdown.rendering.tag.tagBuilder
import eu.iamgio.quarkdown.util.toPlainText

/**
 * A renderer for vanilla Markdown ([eu.iamgio.quarkdown.flavor.base.BaseMarkdownFlavor]) nodes that exports their content into valid HTML code.
 * @param context additional information produced by the earlier stages of the pipeline
 */
open class BaseHtmlNodeRenderer(context: Context) : TagNodeRenderer<HtmlTagBuilder>(context) {
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

    override fun visit(node: AstRoot) = node.children.joinToString(separator = "") { it.accept(this) }

    // Block

    override fun visit(node: Newline) = ""

    override fun visit(node: Code) =
        buildTag("pre") {
            tag("code") {
                +escapeCriticalContent(node.content)
            }
                .optionalAttribute("class", node.language?.let { "language-$it" })
        }

    override fun visit(node: HorizontalRule) =
        tagBuilder("hr")
            .void(true)
            .build()

    override fun visit(node: Heading) = buildTag("h${node.depth}", node.text)

    override fun visit(node: LinkDefinition) = "" // Not rendered

    override fun visit(node: OrderedList) =
        tagBuilder("ol", node.children)
            .optionalAttribute("start", node.startIndex.takeUnless { it == 1 })
            .build()

    override fun visit(node: UnorderedList) = buildTag("ul", node.children)

    /**
     * Appends the base content of a [ListItem] to an [HtmlTagBuilder],
     * following the loose/tight rendering rules (CommonMark 5.3).
     */
    private fun HtmlTagBuilder.appendListItemContent(node: ListItem) {
        // Loose lists (or items not linked to a list for some reason) are rendered as-is.
        if (node.owner?.isLoose != false) {
            +node.children
            return
        }
        // Tight lists don't wrap paragraphs in <p> tags (CommonMark 5.3).
        node.children.forEach {
            if (it is Paragraph) {
                +it.text
            } else {
                +it
            }
        }
    }

    override fun visit(node: BaseListItem) =
        buildTag("li") {
            appendListItemContent(node)
        }

    override fun visit(node: TaskListItem) =
        buildTag("li") {
            // GFM 5.3 extension.
            +visit(CheckBox(node.isChecked))

            appendListItemContent(node)
        }

    override fun visit(node: Html) = node.content

    override fun visit(node: Table) =
        buildTag("table") {
            // Tables are stored by columns and here transposed to a row-based structure.
            val header = tag("thead")
            val headerRow = header.tag("tr")
            val body = tag("tbody")
            val bodyRows = mutableListOf<HtmlTagBuilder>()

            node.columns.forEach { column ->
                // Value to assign to the 'align' attribute for each cell of this column.
                val alignment = column.alignment.takeUnless { it == Table.Alignment.NONE }?.name?.lowercase()

                // Header cell.
                headerRow.tag("th", column.header.text)
                    .optionalAttribute("align", alignment)

                // Body cells.
                column.cells.forEachIndexed { index, cell ->
                    // Adding a new row if needed.
                    if (index >= bodyRows.size) {
                        bodyRows += body.tag("tr")
                    }
                    // Adding a cell.
                    bodyRows[index].tag("td", cell.text)
                        .optionalAttribute("align", alignment)
                }
            }
        }

    override fun visit(node: Paragraph) = buildTag("p", node.text)

    override fun visit(node: BlockQuote) = buildTag("blockquote", node.children)

    override fun visit(node: BlockText) = "" // Fallback block, should not happen

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

    override fun visit(node: Image) =
        tagBuilder("img")
            .attribute("src", node.link.url)
            .attribute("alt", node.link.label.toPlainText(renderer = this)) // Emphasis is discarded (CommonMark 6.4)
            .optionalAttribute("title", node.link.title)
            .optionalAttribute("width", node.width)
            .optionalAttribute("height", node.height)
            .void(true)
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

    override fun visit(node: PageBreak): CharSequence = throw UnsupportedRenderException(node)

    override fun visit(node: Math): CharSequence = throw UnsupportedRenderException(node)

    override fun visit(node: Aligned): CharSequence = throw UnsupportedRenderException(node)

    override fun visit(node: Stacked): CharSequence = throw UnsupportedRenderException(node)

    override fun visit(node: Clipped): CharSequence = throw UnsupportedRenderException(node)

    override fun visit(node: Box): CharSequence = throw UnsupportedRenderException(node)

    override fun visit(node: PageMarginContentInitializer): CharSequence = throw UnsupportedRenderException(node)

    override fun visit(node: PageCounterInitializer): CharSequence = throw UnsupportedRenderException(node)

    override fun visit(node: SlidesConfigurationInitializer): CharSequence = throw UnsupportedRenderException(node)

    override fun visit(node: MathSpan): CharSequence = throw UnsupportedRenderException(node)

    override fun visit(node: TextTransform): CharSequence = throw UnsupportedRenderException(node)
}
