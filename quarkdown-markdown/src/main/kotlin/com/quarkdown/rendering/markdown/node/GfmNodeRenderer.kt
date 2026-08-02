package com.quarkdown.rendering.markdown.node

import com.quarkdown.core.ast.InlineContent
import com.quarkdown.core.ast.Node
import com.quarkdown.core.ast.attributes.id.getId
import com.quarkdown.core.ast.base.block.BlockQuote
import com.quarkdown.core.ast.base.block.Code
import com.quarkdown.core.ast.base.block.FootnoteDefinition
import com.quarkdown.core.ast.base.block.Heading
import com.quarkdown.core.ast.base.block.HorizontalRule
import com.quarkdown.core.ast.base.block.Html
import com.quarkdown.core.ast.base.block.LinkDefinition
import com.quarkdown.core.ast.base.block.Table
import com.quarkdown.core.ast.base.block.list.ListItem
import com.quarkdown.core.ast.base.block.list.OrderedList
import com.quarkdown.core.ast.base.block.list.UnorderedList
import com.quarkdown.core.ast.base.inline.CodeSpan
import com.quarkdown.core.ast.base.inline.CriticalContent
import com.quarkdown.core.ast.base.inline.Emphasis
import com.quarkdown.core.ast.base.inline.Image
import com.quarkdown.core.ast.base.inline.LineBreak
import com.quarkdown.core.ast.base.inline.Link
import com.quarkdown.core.ast.base.inline.ReferenceFootnote
import com.quarkdown.core.ast.base.inline.ReferenceImage
import com.quarkdown.core.ast.base.inline.SoftBreak
import com.quarkdown.core.ast.base.inline.Strikethrough
import com.quarkdown.core.ast.base.inline.Strong
import com.quarkdown.core.ast.base.inline.StrongEmphasis
import com.quarkdown.core.ast.base.inline.Text
import com.quarkdown.core.ast.quarkdown.block.Box
import com.quarkdown.core.ast.quarkdown.block.Math
import com.quarkdown.core.ast.quarkdown.block.MermaidDiagram
import com.quarkdown.core.ast.quarkdown.inline.Keybinding
import com.quarkdown.core.ast.quarkdown.inline.MathSpan
import com.quarkdown.core.context.Context
import com.quarkdown.core.context.toc.TableOfContents
import com.quarkdown.core.rendering.tag.buildTag
import com.quarkdown.core.rendering.textual.TextualNodeRenderer
import com.quarkdown.core.util.node.toPlainText
import com.quarkdown.rendering.html.HtmlIdentifierProvider
import com.quarkdown.rendering.html.node.BaseHtmlNodeRenderer

/**
 * Node renderer that converts the AST to GitHub Flavored Markdown (GFM).
 *
 * Standard CommonMark and GFM constructs are emitted with their native syntax
 * (`#` headings, `**strong**`, fenced code blocks, GFM tables, task list items, ...).
 * Quarkdown-specific nodes without a direct Markdown equivalent degrade gracefully:
 * their inner content is emitted where meaningful, while purely presentational or
 * document-structural nodes render as empty strings.
 */
class GfmNodeRenderer(
    context: Context,
) : TextualNodeRenderer(context) {
    private val html = BaseHtmlNodeRenderer(context)

    private val ids = HtmlIdentifierProvider.of(renderer = this, context = context)

    override fun tableOfContentsItemUrl(item: TableOfContents.Item): String = "#" + ids.getId(item.target)

    private fun escapeText(text: String): String = text.replace(TEXT_ESCAPE_REGEX, "\\\\$1")

    override fun visit(node: Text) = escapeText(node.text)

    override fun visit(node: CriticalContent) = escapeText(node.text)

    /**
     * Formats a Markdown link title as ` "title"`, or an empty string if [title] is null or empty.
     */
    private fun formatTitle(title: InlineContent?): String {
        val rendered = title?.visitAll().orEmpty()
        return if (rendered.isEmpty()) "" else " \"${rendered.replace("\"", "\\\"")}\""
    }

    override fun visit(node: Code) =
        buildString {
            val longestRun = Regex("`+").findAll(node.content).maxOfOrNull { it.value.length } ?: 0
            val fence = "`".repeat(maxOf(3, longestRun + 1))
            append(fence)
            node.language?.let(::append)
            append('\n')
            append(node.content)
            if (!node.content.endsWith('\n')) append('\n')
            append(fence)
            node.caption?.visitAll()?.let { append("\n", it) }
        }.blockNode

    override fun visit(node: HorizontalRule) = "---".blockNode

    override fun visit(node: Heading): CharSequence {
        val text = node.text.visitAll()
        if (text.isBlank()) return ""

        val depth = node.depth.coerceIn(Heading.MIN_DEPTH, Heading.MAX_DEPTH)
        return when (val id = node.customId) {
            null -> {
                "${"#".repeat(depth)} $text"
            }

            else -> {
                html.buildTag("h$depth") {
                    attribute("id", id)
                    +text
                }
            }
        }.blockNode
    }

    override fun visit(node: LinkDefinition) =
        buildString {
            append("[")
            append(node.label.visitAll())
            append("]: ")
            append(node.url)
            append(formatTitle(node.title))
        }.blockNode

    override fun renderFootnoteDefinition(node: FootnoteDefinition) = ("[^${node.label}]: " + node.text.visitAll()).blockNode

    override fun visit(node: OrderedList) =
        buildString {
            node.children.forEachIndexed { index, item ->
                append(renderListItem(item, "${index + node.startIndex}. "))
                if (node.isLoose) appendLine()
            }
        }.blockNode

    override fun visit(node: UnorderedList) =
        buildString {
            node.children.forEach { item ->
                append(renderListItem(item, "- "))
                if (node.isLoose) appendLine()
            }
        }.blockNode

    private fun renderListItem(
        item: Node,
        marker: String,
    ): String {
        val indent = " ".repeat(marker.length)
        val rendered = item.accept(this).toString().trimEnd()
        return buildString {
            rendered.lines().forEachIndexed { index, line ->
                when {
                    index == 0 -> {
                        append(marker).append(line)
                    }

                    line.isBlank() -> {}

                    else -> {
                        append(indent).append(line)
                    }
                }
                append('\n')
            }
        }
    }

    override fun visit(node: ListItem) = node.visitChildren().toString().trimEnd()

    override fun visit(node: Html) = ""

    override fun visit(node: Table) =
        buildString {
            val columns = node.columns
            if (columns.isEmpty()) return@buildString

            fun renderCells(cells: List<InlineContent>) {
                append("|")
                cells.forEach { cell ->
                    append(' ')
                    append(
                        cell
                            .visitAll()
                            .replace("|", "\\|")
                            .replace(Regex("\\s*\\n\\s*"), " "),
                    )
                    append(" |")
                }
                append('\n')
            }

            renderCells(columns.map { it.header.text })

            append("|")
            columns.forEach { column ->
                val separator =
                    when (column.alignment) {
                        Table.Alignment.LEFT -> " :--- |"
                        Table.Alignment.CENTER -> " :---: |"
                        Table.Alignment.RIGHT -> " ---: |"
                        Table.Alignment.NONE -> " --- |"
                    }
                append(separator)
            }
            append('\n')

            val rowCount = columns.maxOfOrNull { it.cells.size } ?: 0
            for (rowIndex in 0 until rowCount) {
                renderCells(
                    columns.map { column ->
                        column.cells.getOrNull(rowIndex)?.text ?: emptyList()
                    },
                )
            }

            node.caption?.visitAll()?.let { append('\n', it) }
        }.blockNode

    override fun visit(node: BlockQuote): CharSequence {
        val body = node.content.visitAll().trimEnd()
        val attribution = node.attribution?.visitAll()
        val quoted =
            buildString {
                append(body.replace("\n", "\n> ").let { "> $it" })
                if (!attribution.isNullOrEmpty()) {
                    append("\n> \n> -- ")
                    append(attribution)
                }
            }
        return quoted.blockNode
    }

    override fun visit(node: LineBreak) = "  \n"

    override fun visit(node: SoftBreak) = "\n"

    override fun visitTransformed(node: Link) =
        buildString {
            append("[")
            append(node.label.visitAll())
            append("](")
            append(node.url)
            append(formatTitle(node.title))
            append(")")
        }

    override fun visit(node: ReferenceFootnote) = "[^${node.label}]"

    override fun visitTransformed(node: Image): CharSequence {
        val alt = node.link.label.toPlainText(this)
        val hasSize = node.width != null || node.height != null
        if (hasSize) {
            return html.buildTag("img") {
                attribute("src", node.link.url)
                attribute("alt", alt)
                optionalAttribute("width", node.width)
                optionalAttribute("height", node.height)
                void(true)
            }
        }
        return buildString {
            append("![")
            append(alt)
            append("](")
            append(node.link.url)
            append(formatTitle(node.link.title))
            append(")")
        }
    }

    override fun visit(node: ReferenceImage) = "![${node.link.label.toPlainText(this)}][${node.link.referenceLabel.toPlainText(this)}]"

    override fun visit(node: CodeSpan) = "`${node.text}`"

    override fun visit(node: Emphasis) = "*${node.visitChildren()}*"

    override fun visit(node: Strong) = "**${node.visitChildren()}**"

    override fun visit(node: StrongEmphasis) = "***${node.visitChildren()}***"

    override fun visit(node: Strikethrough) = "~~${node.visitChildren()}~~"

    override fun visit(node: Math) = $$"$$$${node.expression.trim()}$$".blockNode

    override fun visit(node: MathSpan) = $$"$$${node.expression}$"

    override fun visit(node: MermaidDiagram) = visit(Code(content = node.code, language = "mermaid"))

    override fun visit(node: Box) =
        buildString {
            val body = node.content.visitAll().trimEnd()
            node.title?.visitAll()?.let { title ->
                append("> **")
                append(title)
                append("**\n> \n")
            }
            append("> ")
            append(body.replace("\n", "\n> "))
        }.blockNode

    override fun visit(node: Keybinding) =
        node.parts.joinToString(separator = "+") { part ->
            val label =
                when {
                    part.displayName == part.macDisplayName -> part.displayName
                    part is Keybinding.ShiftModifier -> part.displayName
                    else -> "${part.displayName}/${part.macDisplayName}"
                }
            "<kbd>$label</kbd>"
        }

    companion object {
        private val TEXT_ESCAPE_REGEX = Regex("""([\\`*_\[<$])""")
    }
}
