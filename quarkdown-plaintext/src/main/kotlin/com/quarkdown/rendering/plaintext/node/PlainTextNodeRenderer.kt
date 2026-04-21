package com.quarkdown.rendering.plaintext.node

import com.quarkdown.core.ast.AstRoot
import com.quarkdown.core.ast.InlineContent
import com.quarkdown.core.ast.NestableNode
import com.quarkdown.core.ast.attributes.localization.LocalizedKind
import com.quarkdown.core.ast.attributes.location.LocationTrackableNode
import com.quarkdown.core.ast.attributes.location.getLocationLabel
import com.quarkdown.core.ast.attributes.reference.getCitationLabel
import com.quarkdown.core.ast.attributes.reference.getDefinition
import com.quarkdown.core.ast.base.TextNode
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
import com.quarkdown.core.ast.base.block.list.ListItem
import com.quarkdown.core.ast.base.block.list.OrderedList
import com.quarkdown.core.ast.base.block.list.UnorderedList
import com.quarkdown.core.ast.base.inline.CheckBox
import com.quarkdown.core.ast.base.inline.CodeSpan
import com.quarkdown.core.ast.base.inline.Comment
import com.quarkdown.core.ast.base.inline.CriticalContent
import com.quarkdown.core.ast.base.inline.Emphasis
import com.quarkdown.core.ast.base.inline.Image
import com.quarkdown.core.ast.base.inline.LineBreak
import com.quarkdown.core.ast.base.inline.Link
import com.quarkdown.core.ast.base.inline.ReferenceFootnote
import com.quarkdown.core.ast.base.inline.ReferenceImage
import com.quarkdown.core.ast.base.inline.Strikethrough
import com.quarkdown.core.ast.base.inline.Strong
import com.quarkdown.core.ast.base.inline.StrongEmphasis
import com.quarkdown.core.ast.base.inline.SubdocumentLink
import com.quarkdown.core.ast.base.inline.Text
import com.quarkdown.core.ast.dsl.buildBlock
import com.quarkdown.core.ast.parallelAcceptAll
import com.quarkdown.core.ast.quarkdown.CaptionableNode
import com.quarkdown.core.ast.quarkdown.FunctionCallNode
import com.quarkdown.core.ast.quarkdown.bibliography.BibliographyCitation
import com.quarkdown.core.ast.quarkdown.bibliography.BibliographyView
import com.quarkdown.core.ast.quarkdown.block.Box
import com.quarkdown.core.ast.quarkdown.block.Clipped
import com.quarkdown.core.ast.quarkdown.block.Collapse
import com.quarkdown.core.ast.quarkdown.block.Container
import com.quarkdown.core.ast.quarkdown.block.Figure
import com.quarkdown.core.ast.quarkdown.block.FileTree
import com.quarkdown.core.ast.quarkdown.block.FileTreeEntry
import com.quarkdown.core.ast.quarkdown.block.Landscape
import com.quarkdown.core.ast.quarkdown.block.Math
import com.quarkdown.core.ast.quarkdown.block.MermaidDiagram
import com.quarkdown.core.ast.quarkdown.block.NavigationContainer
import com.quarkdown.core.ast.quarkdown.block.Numbered
import com.quarkdown.core.ast.quarkdown.block.PageBreak
import com.quarkdown.core.ast.quarkdown.block.SlidesFragment
import com.quarkdown.core.ast.quarkdown.block.SlidesSpeakerNote
import com.quarkdown.core.ast.quarkdown.block.Stacked
import com.quarkdown.core.ast.quarkdown.block.SubdocumentGraph
import com.quarkdown.core.ast.quarkdown.block.toc.TableOfContentsView
import com.quarkdown.core.ast.quarkdown.block.toc.convertTableOfContentsToListNode
import com.quarkdown.core.ast.quarkdown.inline.IconImage
import com.quarkdown.core.ast.quarkdown.inline.InlineCollapse
import com.quarkdown.core.ast.quarkdown.inline.Keybinding
import com.quarkdown.core.ast.quarkdown.inline.LastHeading
import com.quarkdown.core.ast.quarkdown.inline.MathSpan
import com.quarkdown.core.ast.quarkdown.inline.PageCounter
import com.quarkdown.core.ast.quarkdown.inline.TextSymbol
import com.quarkdown.core.ast.quarkdown.inline.TextTransform
import com.quarkdown.core.ast.quarkdown.inline.Whitespace
import com.quarkdown.core.ast.quarkdown.invisible.PageMarginContentInitializer
import com.quarkdown.core.ast.quarkdown.invisible.PageNumberFormatter
import com.quarkdown.core.ast.quarkdown.invisible.PageNumberReset
import com.quarkdown.core.ast.quarkdown.invisible.SlidesConfigurationInitializer
import com.quarkdown.core.ast.quarkdown.reference.CrossReference
import com.quarkdown.core.ast.quarkdown.reference.CrossReferenceableNode
import com.quarkdown.core.context.Context
import com.quarkdown.core.context.localization.localizeOrNull
import com.quarkdown.core.rendering.NodeRenderer
import com.quarkdown.core.util.indent

/**
 * Node renderer that converts the AST to plain text.
 * It omits non-textual elements and formats structural elements appropriately.
 */
class PlainTextNodeRenderer(
    context: Context,
) : NodeRenderer(context) {
    private fun NestableNode.visitChildren() = children.visitAll()

    private fun InlineContent.visitAll() = parallelAcceptAll(this@PlainTextNodeRenderer).joinToString(separator = "")

    private val String.blockNode: String
        get() =
            when {
                endsWith("\n\n") -> this
                endsWith('\n') -> this + "\n"
                else -> this + "\n\n"
            }

    override fun createMediaPassthroughPrefixReplacement(): String = "."

    override fun visit(node: AstRoot) = node.visitChildren()

    override fun visit(node: Newline) = ""

    override fun visit(node: Code) =
        buildString {
            append(node.content.indent("\t"))
            node.caption?.visitAll()?.let { append("\n", it) }
        }.blockNode

    override fun visit(node: HorizontalRule) = "-----".blockNode

    override fun visit(node: Heading) = node.visitChildren().blockNode

    override fun visit(node: LinkDefinition) = ""

    override fun visit(node: FootnoteDefinition) = ""

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

    override fun visit(node: ListItem) = node.visitChildren().indent("\t")

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

    override fun visit(node: Paragraph) = node.visitChildren().blockNode

    override fun visit(node: BlockQuote) = "> ${node.content.visitAll().trimEnd().replace("\n", "\n> ")}".blockNode

    override fun visit(node: BlankNode) = ""

    override fun visit(node: Comment) = ""

    override fun visit(node: LineBreak) = "\n"

    override fun visit(node: CriticalContent) = node.text

    override fun visitTransformed(node: Link) = node.visitChildren()

    override fun visit(node: SubdocumentLink) = visit(node.link)

    override fun visit(node: ReferenceFootnote) = "" // Footnotes are currently unsupported

    override fun visitTransformed(node: Image) = ""

    override fun visit(node: ReferenceImage) = ""

    override fun visit(node: CheckBox) = if (node.isChecked) "[x] " else "[ ] "

    override fun visit(node: Text) = node.text

    override fun visit(node: TextSymbol) = node.text

    override fun visit(node: CodeSpan) = node.text

    override fun visit(node: Emphasis) = node.visitChildren()

    override fun visit(node: Strong) = node.visitChildren()

    override fun visit(node: StrongEmphasis) = node.visitChildren()

    override fun visit(node: Strikethrough) = node.visitChildren()

    override fun visit(node: FunctionCallNode) = node.visitChildren()

    override fun visit(node: Figure<*>) =
        buildString {
            append(node.child.accept(this@PlainTextNodeRenderer))
            node.caption?.visitAll()?.let { append("\n", it) }
        }.blockNode

    override fun visit(node: PageBreak) = ""

    override fun visit(node: Math) = node.expression.trim().blockNode

    override fun visit(node: Container) = node.visitChildren().blockNode

    override fun visit(node: Stacked) = node.visitChildren().blockNode

    override fun visit(node: Numbered) = node.visitChildren()

    override fun visit(node: Landscape) = node.visitChildren()

    override fun visit(node: Clipped) = node.visitChildren()

    override fun visit(node: Box) = ((node.title?.visitAll()?.plus("\n-----\n") ?: "") + node.content.visitAll()).blockNode

    override fun visit(node: Collapse) = node.content.visitAll()

    override fun visit(node: Whitespace) = ""

    override fun visit(node: NavigationContainer) = node.visitChildren()

    override fun visit(node: TableOfContentsView): CharSequence {
        val tableOfContents = context.attributes.tableOfContents ?: return ""

        val list =
            convertTableOfContentsToListNode(
                node,
                this@PlainTextNodeRenderer,
                tableOfContents.items,
                loose = false,
                wrapLinksInParagraphs = true,
                linkUrlMapper = { "" },
            )

        return list.accept(this).toString().blockNode
    }

    override fun visit(node: BibliographyView): CharSequence =
        buildString {
            node.bibliography.entries.values.forEachIndexed { index, entry ->
                append(node.style.labelProvider.getListLabel(entry, index))
                append(" ")
                append(
                    node.style
                        .contentOf(entry)
                        .visitAll(),
                )
                appendLine()
            }
        }.blockNode

    override fun visit(node: MermaidDiagram) = ""

    override fun visit(node: FileTree): CharSequence {
        val list =
            buildBlock {
                unorderedList(loose = false) {
                    node.entries.forEach { entry ->
                        listItem {
                            when (entry) {
                                is FileTreeEntry.File -> {
                                    paragraph { text(entry.name) }
                                }

                                is FileTreeEntry.Directory -> {
                                    paragraph { text(entry.name + "/") }
                                    +FileTree(entry.entries)
                                }

                                is FileTreeEntry.Ellipsis -> {
                                    paragraph { text("...") }
                                }
                            }
                        }
                    }
                }
            }
        return list.accept(this).toString().blockNode
    }

    override fun visit(node: SubdocumentGraph) = ""

    override fun visit(node: MathSpan) = node.expression

    override fun visit(node: TextTransform) = node.visitChildren()

    override fun visit(node: IconImage) = ""

    override fun visit(node: InlineCollapse) = node.visitChildren()

    override fun visit(node: Keybinding) =
        node.parts.joinToString(separator = "+") {
            when {
                it.displayName == it.macDisplayName -> it.displayName
                it is Keybinding.ShiftModifier -> it.displayName
                else -> "${it.displayName}/${it.macDisplayName}"
            }
        }

    override fun visit(node: PageCounter) = ""

    override fun visit(node: LastHeading) = ""

    override fun visit(node: CrossReference): CharSequence {
        val definition: CrossReferenceableNode = node.getDefinition(context) ?: return Text("[???]").accept(this)
        val builder = StringBuilder()

        val content =
            when (definition) {
                is LocationTrackableNode if definition.getLocationLabel(context) != null -> {
                    definition.getLocationLabel(context)
                }

                // If no label is available, use the caption if possible.
                is CaptionableNode if definition.caption != null -> {
                    definition.caption!!.visitAll()
                }

                // Fallback: use the target's text if possible.
                is TextNode -> {
                    definition.text
                }

                // Fallback: raw reference ID.
                else -> {
                    node.referenceId
                }
            }

        if (definition is LocalizedKind) {
            val localizedKind = context.localizeOrNull(key = definition.kindLocalizationKey)
            localizedKind?.let(builder::append)
            builder.append(' ')
        }

        builder.append(content)
        return builder.toString()
    }

    override fun visit(node: BibliographyCitation): CharSequence = node.getCitationLabel(context) ?: "[???]"

    override fun visit(node: SlidesFragment) = ""

    override fun visit(node: SlidesSpeakerNote) = ""

    override fun visit(node: PageMarginContentInitializer) = ""

    override fun visit(node: PageNumberFormatter) = ""

    override fun visit(node: PageNumberReset) = ""

    override fun visit(node: SlidesConfigurationInitializer) = ""
}
