package com.quarkdown.core.ast.rewriter

import com.quarkdown.core.ast.AstRoot
import com.quarkdown.core.ast.NestableNode
import com.quarkdown.core.ast.Node
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
import com.quarkdown.core.ast.base.block.diverge
import com.quarkdown.core.ast.base.block.list.ListItem
import com.quarkdown.core.ast.base.block.list.OrderedList
import com.quarkdown.core.ast.base.block.list.UnorderedList
import com.quarkdown.core.ast.base.block.list.diverge
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
import com.quarkdown.core.ast.base.inline.SoftBreak
import com.quarkdown.core.ast.base.inline.Strikethrough
import com.quarkdown.core.ast.base.inline.Strong
import com.quarkdown.core.ast.base.inline.StrongEmphasis
import com.quarkdown.core.ast.base.inline.SubdocumentLink
import com.quarkdown.core.ast.base.inline.Text
import com.quarkdown.core.ast.base.inline.diverge
import com.quarkdown.core.ast.diverge
import com.quarkdown.core.ast.quarkdown.FunctionCallNode
import com.quarkdown.core.ast.quarkdown.bibliography.BibliographyCitation
import com.quarkdown.core.ast.quarkdown.bibliography.BibliographyView
import com.quarkdown.core.ast.quarkdown.block.Box
import com.quarkdown.core.ast.quarkdown.block.Clipped
import com.quarkdown.core.ast.quarkdown.block.Collapse
import com.quarkdown.core.ast.quarkdown.block.Container
import com.quarkdown.core.ast.quarkdown.block.Figure
import com.quarkdown.core.ast.quarkdown.block.FileTree
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
import com.quarkdown.core.ast.quarkdown.block.diverge
import com.quarkdown.core.ast.quarkdown.block.toc.TableOfContentsView
import com.quarkdown.core.ast.quarkdown.inline.IconImage
import com.quarkdown.core.ast.quarkdown.inline.InlineCollapse
import com.quarkdown.core.ast.quarkdown.inline.Keybinding
import com.quarkdown.core.ast.quarkdown.inline.LastHeading
import com.quarkdown.core.ast.quarkdown.inline.MathSpan
import com.quarkdown.core.ast.quarkdown.inline.PageCounter
import com.quarkdown.core.ast.quarkdown.inline.TextSymbol
import com.quarkdown.core.ast.quarkdown.inline.TextTransform
import com.quarkdown.core.ast.quarkdown.inline.Whitespace
import com.quarkdown.core.ast.quarkdown.inline.diverge
import com.quarkdown.core.ast.quarkdown.invisible.PageMarginContentInitializer
import com.quarkdown.core.ast.quarkdown.invisible.PageNumberFormatter
import com.quarkdown.core.ast.quarkdown.invisible.PageNumberReset
import com.quarkdown.core.ast.quarkdown.invisible.SlidesConfigurationInitializer
import com.quarkdown.core.ast.quarkdown.invisible.diverge
import com.quarkdown.core.ast.quarkdown.reference.CrossReference
import com.quarkdown.core.visitor.node.NodeVisitor

/**
 * Returns a copy of this node with its children replaced by [newChildren].
 *
 * For most [NestableNode]s, the copy preserves all other parameters of the original node
 * and only swaps the children/text slot.
 *
 * For leaf nodes (with no children to replace), the original node is returned unchanged
 * and [newChildren] is ignored.
 *
 * @param newChildren the list of nodes that should replace this node's children
 * @return a copy of this node with [newChildren] in place of its original children,
 *         or this node itself if it has no children
 */
fun Node.withChildren(newChildren: List<Node>): Node = accept(NodeNewChildrenVisitor(newChildren))

/**
 * A [NodeVisitor] that, given a list of replacement [newChildren],
 * returns a copy of the visited node with its children swapped for [newChildren].
 *
 * Nestable and text nodes whose children/text is exposed as a primary constructor parameter
 * delegate to the `diverge` extension generated by the `@Diverge` annotation.
 *
 * Leaf nodes (with no children to replace) are returned unchanged.
 */
private class NodeNewChildrenVisitor(
    private val newChildren: List<Node>,
) : NodeVisitor<Node> {
    override fun visit(node: AstRoot) = node.diverge(newChildren)

    override fun visit(node: Newline) = node

    override fun visit(node: Code) = node

    override fun visit(node: HorizontalRule) = node

    override fun visit(node: Heading) = node.diverge(newChildren)

    override fun visit(node: LinkDefinition) = node.diverge(newChildren)

    override fun visit(node: FootnoteDefinition) = node.diverge(newChildren)

    override fun visit(node: OrderedList) = node.diverge(newChildren)

    override fun visit(node: UnorderedList) = node.diverge(newChildren)

    override fun visit(node: ListItem) = node.diverge(newChildren)

    override fun visit(node: Html) = node

    override fun visit(node: Table) = node

    override fun visit(node: Paragraph) = node.diverge(newChildren)

    // BlockQuote exposes content + attribution as children
    override fun visit(node: BlockQuote) = node.diverge(newChildren.take(node.content.size))

    override fun visit(node: BlankNode) = node

    override fun visit(node: Comment) = node

    override fun visit(node: LineBreak) = node

    override fun visit(node: SoftBreak) = node

    override fun visit(node: CriticalContent) = node

    override fun visit(node: Link) = node.diverge(newChildren)

    override fun visit(node: ReferenceLink) = node.diverge(newChildren)

    override fun visit(node: SubdocumentLink) = node.diverge(link = node.link.diverge(newChildren))

    override fun visit(node: ReferenceFootnote) = node

    override fun visit(node: Image) = node

    override fun visit(node: ReferenceImage) = node

    override fun visit(node: CheckBox) = node

    override fun visit(node: Text) = node

    override fun visit(node: TextSymbol) = node

    override fun visit(node: CodeSpan) = node

    override fun visit(node: Emphasis) = node.diverge(newChildren)

    override fun visit(node: Strong) = node.diverge(newChildren)

    override fun visit(node: StrongEmphasis) = node.diverge(newChildren)

    override fun visit(node: Strikethrough) = node.diverge(newChildren)

    // FunctionCallNode's children are a mutable list.
    override fun visit(node: FunctionCallNode) =
        node.also {
            it.children.clear()
            it.children.addAll(newChildren)
        }

    @Suppress("UNCHECKED_CAST")
    override fun visit(node: Figure<*>) = (node as Figure<Node>).diverge(child = newChildren.first())

    override fun visit(node: PageBreak) = node

    override fun visit(node: Math) = node

    override fun visit(node: Container) = node.diverge(newChildren)

    override fun visit(node: Stacked) = node.diverge(newChildren)

    override fun visit(node: Numbered) = node

    override fun visit(node: Landscape) = node.diverge(newChildren)

    override fun visit(node: Clipped) = node.diverge(newChildren)

    override fun visit(node: Box) = node.diverge(newChildren)

    override fun visit(node: Collapse) = node.diverge(newChildren)

    override fun visit(node: Whitespace) = node

    override fun visit(node: NavigationContainer) = node.diverge(newChildren)

    override fun visit(node: TableOfContentsView) = node

    override fun visit(node: BibliographyView) = node

    override fun visit(node: MermaidDiagram) = node

    override fun visit(node: FileTree) = node

    override fun visit(node: SubdocumentGraph) = node

    override fun visit(node: MathSpan) = node

    override fun visit(node: TextTransform) = node.diverge(newChildren)

    override fun visit(node: IconImage) = node

    override fun visit(node: InlineCollapse) = node.diverge(newChildren)

    override fun visit(node: Keybinding) = node

    override fun visit(node: PageCounter) = node

    override fun visit(node: LastHeading) = node

    override fun visit(node: CrossReference) = node

    override fun visit(node: BibliographyCitation) = node

    override fun visit(node: SlidesFragment) = node.diverge(newChildren)

    override fun visit(node: SlidesSpeakerNote) = node.diverge(newChildren)

    override fun visit(node: PageMarginContentInitializer) = node.diverge(newChildren)

    override fun visit(node: PageNumberFormatter) = node

    override fun visit(node: PageNumberReset) = node

    override fun visit(node: SlidesConfigurationInitializer) = node
}
