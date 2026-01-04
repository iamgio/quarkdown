package com.quarkdown.rendering.plaintext.node

import com.quarkdown.core.ast.AstRoot
import com.quarkdown.core.ast.InlineContent
import com.quarkdown.core.ast.NestableNode
import com.quarkdown.core.ast.attributes.localization.LocalizedKind
import com.quarkdown.core.ast.attributes.location.LocationTrackableNode
import com.quarkdown.core.ast.attributes.location.getLocationLabel
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
import com.quarkdown.core.ast.base.inline.ReferenceLink
import com.quarkdown.core.ast.base.inline.Strikethrough
import com.quarkdown.core.ast.base.inline.Strong
import com.quarkdown.core.ast.base.inline.StrongEmphasis
import com.quarkdown.core.ast.base.inline.SubdocumentLink
import com.quarkdown.core.ast.base.inline.Text
import com.quarkdown.core.ast.dsl.buildInline
import com.quarkdown.core.ast.quarkdown.CaptionableNode
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
import com.quarkdown.core.ast.quarkdown.block.toc.TableOfContentsView
import com.quarkdown.core.ast.quarkdown.block.toc.convertTableOfContentsToListNode
import com.quarkdown.core.ast.quarkdown.inline.InlineCollapse
import com.quarkdown.core.ast.quarkdown.inline.LastHeading
import com.quarkdown.core.ast.quarkdown.inline.MathSpan
import com.quarkdown.core.ast.quarkdown.inline.PageCounter
import com.quarkdown.core.ast.quarkdown.inline.TextSymbol
import com.quarkdown.core.ast.quarkdown.inline.TextTransform
import com.quarkdown.core.ast.quarkdown.inline.Whitespace
import com.quarkdown.core.ast.quarkdown.invisible.PageMarginContentInitializer
import com.quarkdown.core.ast.quarkdown.invisible.PageNumberReset
import com.quarkdown.core.ast.quarkdown.invisible.SlidesConfigurationInitializer
import com.quarkdown.core.ast.quarkdown.reference.CrossReference
import com.quarkdown.core.ast.quarkdown.reference.CrossReferenceableNode
import com.quarkdown.core.bibliography.BibliographyEntry
import com.quarkdown.core.bibliography.style.getContent
import com.quarkdown.core.context.Context
import com.quarkdown.core.context.localization.localizeOrNull
import com.quarkdown.core.rendering.NodeRenderer
import com.quarkdown.core.util.indent

/**
 * Node renderer that converts the AST to plain text.
 * It omits non-textual elements and formats structural elements appropriately.
 */
class PlainTextNodeRenderer(
    private val context: Context,
) : NodeRenderer {
    private fun NestableNode.visitChildren() = children.visitAll()

    private fun InlineContent.visitAll() = joinToString(separator = "") { it.accept(this@PlainTextNodeRenderer) }

    private val String.blockNode: String
        get() =
            when {
                endsWith("\n\n") -> this
                endsWith('\n') -> this + "\n"
                else -> this + "\n\n"
            }

    override fun visit(node: AstRoot) = node.visitChildren()

    override fun visit(node: Newline) = ""

    override fun visit(node: Code) = node.content.indent("\t").blockNode

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

    override fun visit(node: Table) = node.visitChildren().blockNode // Markdown-like table rendering may be supported later

    override fun visit(node: Paragraph) = node.visitChildren().blockNode

    override fun visit(node: BlockQuote) = "> ${node.visitChildren().trimEnd().replace("\n", "\n> ")}".blockNode

    override fun visit(node: BlankNode) = ""

    override fun visit(node: Comment) = ""

    override fun visit(node: LineBreak) = "\n"

    override fun visit(node: CriticalContent) = node.text

    override fun visit(node: Link) = node.visitChildren()

    override fun visit(node: ReferenceLink) = node.label.visitAll()

    override fun visit(node: SubdocumentLink) = visit(node.link)

    override fun visit(node: ReferenceFootnote) = "" // Footnotes are currently unsupported

    override fun visit(node: Image) = ""

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

    override fun visit(node: Figure<*>) = node.visitChildren()

    override fun visit(node: PageBreak) = ""

    override fun visit(node: Math) = node.expression.trim().blockNode

    override fun visit(node: Container) = node.visitChildren().blockNode

    override fun visit(node: Stacked) = node.visitChildren().blockNode

    override fun visit(node: Numbered) = node.visitChildren()

    override fun visit(node: Landscape) = node.visitChildren()

    override fun visit(node: FullColumnSpan) = node.visitChildren()

    override fun visit(node: Clipped) = node.visitChildren()

    override fun visit(node: Box) = ((node.title?.visitAll()?.plus("\n-----\n") ?: "") + node.visitChildren()).blockNode

    override fun visit(node: Collapse) = node.visitChildren()

    override fun visit(node: Whitespace) = ""

    override fun visit(node: TableOfContentsView): CharSequence {
        val tableOfContents = context.attributes.tableOfContents ?: return ""

        val builder = StringBuilder()

        // Title.
        val title =
            node.title
                ?: context.localizeOrNull(key = "tableofcontents")?.let { buildInline { text(it) } }

        title?.let {
            Heading(
                depth = 1,
                text = it,
            ).accept(this).let(builder::append)
        }

        // Content.
        val list =
            convertTableOfContentsToListNode(
                node,
                this@PlainTextNodeRenderer,
                tableOfContents.items,
                loose = false,
                wrapLinksInParagraphs = true,
                linkUrlMapper = { "" },
            )
        list.accept(this).let(builder::append)

        return builder.toString().blockNode
    }

    override fun visit(node: BibliographyView): CharSequence {
        val builder = StringBuilder()

        // Title.
        val title =
            node.title
                ?: context.localizeOrNull(key = "bibliography")?.let { buildInline { text(it) } }

        title?.let {
            Heading(
                depth = 1,
                text = it,
                isDecorative = node.isTitleDecorative,
            ).accept(this).let(builder::append)
        }

        // Content.
        node.bibliography.entries.values.mapIndexed { index, entry ->
            builder.append(node.style.labelProvider.getLabel(entry, index))
            builder.append(node.style.contentProvider.getContent(entry))
        }

        return builder.toString().blockNode
    }

    override fun visit(node: MermaidDiagram) = ""

    override fun visit(node: SubdocumentGraph) = ""

    override fun visit(node: MathSpan) = node.expression

    override fun visit(node: TextTransform) = node.visitChildren()

    override fun visit(node: InlineCollapse) = node.visitChildren()

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
                    definition.caption!!
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
        }

        builder.append(content)
        return builder.toString()
    }

    override fun visit(node: BibliographyCitation): CharSequence {
        val (entry: BibliographyEntry, view: BibliographyView) =
            node.getDefinition(context) ?: return "[???]"

        val index = view.bibliography.indexOf(entry)
        val label = view.style.labelProvider.getLabel(entry, index)
        return label
    }

    override fun visit(node: SlidesFragment) = ""

    override fun visit(node: SlidesSpeakerNote) = ""

    override fun visit(node: PageMarginContentInitializer) = ""

    override fun visit(node: PageNumberReset) = ""

    override fun visit(node: SlidesConfigurationInitializer) = ""
}
