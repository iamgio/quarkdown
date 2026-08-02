package com.quarkdown.rendering.plaintext.node

import com.quarkdown.core.ast.base.block.BlockQuote
import com.quarkdown.core.ast.base.block.Code
import com.quarkdown.core.ast.base.block.Heading
import com.quarkdown.core.ast.base.block.HorizontalRule
import com.quarkdown.core.ast.base.block.Html
import com.quarkdown.core.ast.base.block.LinkDefinition
import com.quarkdown.core.ast.base.block.Table
import com.quarkdown.core.ast.base.block.list.ListItem
import com.quarkdown.core.ast.base.block.list.OrderedList
import com.quarkdown.core.ast.base.block.list.UnorderedList
import com.quarkdown.core.ast.base.inline.CodeSpan
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
import com.quarkdown.core.ast.base.inline.SubdocumentLink
import com.quarkdown.core.ast.quarkdown.block.Box
import com.quarkdown.core.ast.quarkdown.block.Math
import com.quarkdown.core.ast.quarkdown.block.MermaidDiagram
import com.quarkdown.core.ast.quarkdown.block.SlidesFragment
import com.quarkdown.core.ast.quarkdown.inline.Keybinding
import com.quarkdown.core.ast.quarkdown.inline.MathSpan
import com.quarkdown.core.context.Context
import com.quarkdown.core.localization.isCJK
import com.quarkdown.core.rendering.textual.TextualNodeRenderer
import com.quarkdown.core.util.indent

/**
 * Node renderer that converts the AST to plain text.
 * It omits non-textual elements and formats structural elements appropriately.
 */
class PlainTextNodeRenderer(
    context: Context,
) : TextualNodeRenderer(context) {
    private val isCJK by lazy { context.documentInfo.locale.isCJK() }

    override fun visit(node: Code) =
        buildString {
            append(node.content.indent("\t"))
            node.caption?.visitAll()?.let { append("\n", it) }
        }.blockNode

    override fun visit(node: HorizontalRule) = "-----".blockNode

    override fun visit(node: Heading) = node.visitChildren().toString().blockNode

    override fun visit(node: LinkDefinition) = ""

    override fun visit(node: OrderedList) =
        buildString {
            node.children.forEachIndexed { index, item ->
                append(index + node.startIndex)
                append(". ")
                appendLine(item.accept(this@PlainTextNodeRenderer).trim())
                if (node.isLoose) {
                    appendLine()
                }
            }
        }.blockNode

    override fun visit(node: UnorderedList) =
        buildString {
            node.children.forEach { item ->
                append("- ")
                appendLine(item.accept(this@PlainTextNodeRenderer).trim())
                if (node.isLoose) {
                    appendLine()
                }
            }
        }.blockNode

    override fun visit(node: ListItem) = node.visitChildren().toString().indent("\t")

    override fun visit(node: Html) = ""

    override fun visit(node: Table) =
        buildString {
            append(
                node.columns
                    .asSequence()
                    .flatMap { it.cells + it.header }
                    .flatMap { it.text }
                    .toList()
                    .visitAll(),
            )
            node.caption?.visitAll()?.let { append("\n", it) }
        }.blockNode

    override fun visit(node: BlockQuote) = "> ${node.content.visitAll().trimEnd().replace("\n", "\n> ")}".blockNode

    override fun visit(node: LineBreak) = "\n"

    override fun visit(node: SoftBreak) = if (isCJK) "" else " "

    override fun visitTransformed(node: Link) = node.visitChildren()

    override fun visit(node: SubdocumentLink) = visit(node.link)

    override fun visit(node: ReferenceFootnote) = "" // Footnotes are currently unsupported

    override fun visitTransformed(node: Image) = ""

    override fun visit(node: ReferenceImage) = ""

    override fun visit(node: CodeSpan) = node.text

    override fun visit(node: Emphasis) = node.visitChildren()

    override fun visit(node: Strong) = node.visitChildren()

    override fun visit(node: StrongEmphasis) = node.visitChildren()

    override fun visit(node: Strikethrough) = node.visitChildren()

    override fun visit(node: Math) = node.expression.trim().blockNode

    override fun visit(node: MathSpan) = node.expression

    override fun visit(node: Box) = ((node.title?.visitAll()?.plus("\n-----\n") ?: "") + node.content.visitAll()).blockNode

    override fun visit(node: MermaidDiagram) = ""

    override fun visit(node: Keybinding) =
        node.parts.joinToString(separator = "+") {
            when {
                it.displayName == it.macDisplayName -> it.displayName
                it is Keybinding.ShiftModifier -> it.displayName
                else -> "${it.displayName}/${it.macDisplayName}"
            }
        }

    override fun visit(node: SlidesFragment) = ""
}
