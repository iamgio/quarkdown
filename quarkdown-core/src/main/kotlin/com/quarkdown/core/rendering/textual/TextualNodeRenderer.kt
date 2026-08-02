package com.quarkdown.core.rendering.textual

import com.quarkdown.core.ast.AstGroup
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
import com.quarkdown.core.ast.base.block.FootnoteDefinition
import com.quarkdown.core.ast.base.block.Newline
import com.quarkdown.core.ast.base.block.Paragraph
import com.quarkdown.core.ast.base.inline.CheckBox
import com.quarkdown.core.ast.base.inline.Comment
import com.quarkdown.core.ast.base.inline.CriticalContent
import com.quarkdown.core.ast.base.inline.SubdocumentLink
import com.quarkdown.core.ast.base.inline.Text
import com.quarkdown.core.ast.dsl.buildBlock
import com.quarkdown.core.ast.parallelAcceptAll
import com.quarkdown.core.ast.quarkdown.CaptionableNode
import com.quarkdown.core.ast.quarkdown.FunctionCallNode
import com.quarkdown.core.ast.quarkdown.bibliography.BibliographyCitation
import com.quarkdown.core.ast.quarkdown.bibliography.BibliographyView
import com.quarkdown.core.ast.quarkdown.block.Clipped
import com.quarkdown.core.ast.quarkdown.block.Collapse
import com.quarkdown.core.ast.quarkdown.block.Container
import com.quarkdown.core.ast.quarkdown.block.Figure
import com.quarkdown.core.ast.quarkdown.block.FileTree
import com.quarkdown.core.ast.quarkdown.block.FileTreeEntry
import com.quarkdown.core.ast.quarkdown.block.Landscape
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
import com.quarkdown.core.ast.quarkdown.inline.LastHeading
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
import com.quarkdown.core.context.toc.TableOfContents
import com.quarkdown.core.rendering.NodeRenderer
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Base class for node renderers that produce plain-textual output,
 * either raw text or lightweight markup such as Markdown.
 */
abstract class TextualNodeRenderer(
    context: Context,
) : NodeRenderer(context) {
    // Footnote definitions are queued and rendered as blocks at the end of every AstRoot.
    private val queuedFootnoteDefinitions = ConcurrentLinkedQueue<FootnoteDefinition>()

    /**
     * Renders all children of this [NestableNode] and joins the results.
     */
    protected fun NestableNode.visitChildren(): CharSequence = children.visitAll()

    /**
     * Renders all inline content nodes in parallel and joins them without a separator.
     */
    protected fun InlineContent.visitAll(): String = parallelAcceptAll(this@TextualNodeRenderer).joinToString(separator = "")

    /**
     * Ensures a block-level string is followed by exactly one blank line, so consecutive
     * blocks are visually separated in the output.
     */
    protected val String.blockNode: String
        get() =
            when {
                endsWith("\n\n") -> this
                endsWith('\n') -> this + "\n"
                else -> this + "\n\n"
            }

    override fun createMediaPassthroughPrefixReplacement(): String = "."

    override fun visit(node: FootnoteDefinition): CharSequence {
        queuedFootnoteDefinitions += node
        return ""
    }

    override fun visit(node: AstRoot): CharSequence {
        queuedFootnoteDefinitions.clear()
        val body = node.visitChildren()
        val defs =
            generateSequence { queuedFootnoteDefinitions.poll() }
                .joinToString(separator = "") { renderFootnoteDefinition(it) }
        return body.toString() + defs
    }

    override fun visit(node: AstGroup) = node.visitChildren()

    /**
     * Renders a queued [FootnoteDefinition] as its final block-level output.
     */
    protected open fun renderFootnoteDefinition(node: FootnoteDefinition): CharSequence = ""

    override fun visit(node: Newline) = ""

    override fun visit(node: BlankNode) = ""

    override fun visit(node: Comment) = ""

    override fun visit(node: Paragraph) = node.visitChildren().toString().blockNode

    override fun visit(node: FunctionCallNode) = node.visitChildren()

    override fun visit(node: CriticalContent) = node.text

    override fun visit(node: Text) = node.text

    override fun visit(node: TextSymbol) = node.text

    override fun visit(node: CheckBox) = if (node.isChecked) "[x] " else "[ ] "

    override fun visit(node: SubdocumentLink) = visit(node.link)

    override fun visit(node: Container) = node.visitChildren().toString().blockNode

    override fun visit(node: Stacked) = node.visitChildren().toString().blockNode

    override fun visit(node: Numbered) = node.visitChildren()

    override fun visit(node: Landscape) = node.visitChildren()

    override fun visit(node: Clipped) = node.visitChildren()

    override fun visit(node: Collapse) = node.content.visitAll()

    override fun visit(node: NavigationContainer) = node.visitChildren()

    override fun visit(node: TextTransform) = node.visitChildren()

    override fun visit(node: InlineCollapse) = node.visitChildren()

    override fun visit(node: Whitespace) = ""

    override fun visit(node: IconImage) = ""

    override fun visit(node: PageBreak) = ""

    override fun visit(node: PageCounter) = ""

    override fun visit(node: LastHeading) = ""

    override fun visit(node: SlidesSpeakerNote) = ""

    override fun visit(node: SlidesFragment) = node.visitChildren()

    override fun visit(node: SubdocumentGraph) = ""

    override fun visit(node: PageMarginContentInitializer) = ""

    override fun visit(node: PageNumberFormatter) = ""

    override fun visit(node: PageNumberReset) = ""

    override fun visit(node: SlidesConfigurationInitializer) = ""

    override fun visit(node: Figure<*>) =
        buildString {
            append(node.child.accept(this@TextualNodeRenderer).trimEnd())
            node.caption?.visitAll()?.let { append("\n", it) }
        }.blockNode

    override fun visit(node: TableOfContentsView): CharSequence {
        val tableOfContents = context.attributes.tableOfContents ?: return ""

        val list =
            convertTableOfContentsToListNode(
                node,
                this,
                tableOfContents.items,
                loose = false,
                wrapLinksInParagraphs = true,
                linkUrlMapper = ::tableOfContentsItemUrl,
            )

        return list.accept(this).toString().blockNode
    }

    /**
     * URL a table-of-contents entry links to.
     */
    protected open fun tableOfContentsItemUrl(item: TableOfContents.Item): String = ""

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

    override fun visit(node: BibliographyCitation): CharSequence = node.getCitationLabel(context) ?: "[???]"

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
            context.localizeOrNull(key = definition.kindLocalizationKey)?.let {
                builder.append(it).append(' ')
            }
        }

        builder.append(content)
        return builder.toString()
    }
}
