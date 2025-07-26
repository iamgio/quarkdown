package com.quarkdown.core.visitor.node

import com.quarkdown.core.ast.AstRoot
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
import com.quarkdown.core.ast.base.inline.Text
import com.quarkdown.core.ast.quarkdown.FunctionCallNode
import com.quarkdown.core.ast.quarkdown.bibliography.BibliographyCitation
import com.quarkdown.core.ast.quarkdown.bibliography.BibliographyView
import com.quarkdown.core.ast.quarkdown.block.Box
import com.quarkdown.core.ast.quarkdown.block.Clipped
import com.quarkdown.core.ast.quarkdown.block.Collapse
import com.quarkdown.core.ast.quarkdown.block.Container
import com.quarkdown.core.ast.quarkdown.block.Figure
import com.quarkdown.core.ast.quarkdown.block.FullColumnSpan
import com.quarkdown.core.ast.quarkdown.block.Math
import com.quarkdown.core.ast.quarkdown.block.MermaidDiagram
import com.quarkdown.core.ast.quarkdown.block.Numbered
import com.quarkdown.core.ast.quarkdown.block.PageBreak
import com.quarkdown.core.ast.quarkdown.block.SlidesFragment
import com.quarkdown.core.ast.quarkdown.block.SlidesSpeakerNote
import com.quarkdown.core.ast.quarkdown.block.Stacked
import com.quarkdown.core.ast.quarkdown.block.toc.TableOfContentsView
import com.quarkdown.core.ast.quarkdown.inline.InlineCollapse
import com.quarkdown.core.ast.quarkdown.inline.MathSpan
import com.quarkdown.core.ast.quarkdown.inline.PageCounter
import com.quarkdown.core.ast.quarkdown.inline.TextSymbol
import com.quarkdown.core.ast.quarkdown.inline.TextTransform
import com.quarkdown.core.ast.quarkdown.inline.Whitespace
import com.quarkdown.core.ast.quarkdown.invisible.PageMarginContentInitializer
import com.quarkdown.core.ast.quarkdown.invisible.SlidesConfigurationInitializer

/**
 * A visitor for [com.quarkdown.core.ast.Node]s.
 * @param T output type of the `visit` methods
 */
interface NodeVisitor<T> {
    fun visit(node: AstRoot): T

    // Base block

    fun visit(node: Newline): T

    fun visit(node: Code): T

    fun visit(node: HorizontalRule): T

    fun visit(node: Heading): T

    fun visit(node: LinkDefinition): T

    fun visit(node: FootnoteDefinition): T

    fun visit(node: OrderedList): T

    fun visit(node: UnorderedList): T

    fun visit(node: ListItem): T

    fun visit(node: Html): T

    fun visit(node: Table): T

    fun visit(node: Paragraph): T

    fun visit(node: BlockQuote): T

    fun visit(node: BlankNode): T

    // Base inline

    fun visit(node: Comment): T

    fun visit(node: LineBreak): T

    fun visit(node: CriticalContent): T

    fun visit(node: Link): T

    fun visit(node: ReferenceLink): T

    fun visit(node: ReferenceFootnote): T

    fun visit(node: Image): T

    fun visit(node: ReferenceImage): T

    fun visit(node: CheckBox): T

    fun visit(node: Text): T

    fun visit(node: TextSymbol): T

    fun visit(node: CodeSpan): T

    fun visit(node: Emphasis): T

    fun visit(node: Strong): T

    fun visit(node: StrongEmphasis): T

    fun visit(node: Strikethrough): T

    // Quarkdown extensions

    fun visit(node: FunctionCallNode): T

    // Quarkdown block

    fun visit(node: Figure<*>): T

    fun visit(node: PageBreak): T

    fun visit(node: Math): T

    fun visit(node: Container): T

    fun visit(node: Stacked): T

    fun visit(node: Numbered): T

    fun visit(node: FullColumnSpan): T

    fun visit(node: Clipped): T

    fun visit(node: Box): T

    fun visit(node: Collapse): T

    fun visit(node: Whitespace): T

    fun visit(node: TableOfContentsView): T

    fun visit(node: BibliographyView): T

    fun visit(node: MermaidDiagram): T

    // Quarkdown inline

    fun visit(node: MathSpan): T

    fun visit(node: TextTransform): T

    fun visit(node: InlineCollapse): T

    fun visit(node: PageCounter): T

    fun visit(node: BibliographyCitation): T

    fun visit(node: SlidesFragment): T

    fun visit(node: SlidesSpeakerNote): T

    // Quarkdown invisible nodes

    fun visit(node: PageMarginContentInitializer): T

    fun visit(node: SlidesConfigurationInitializer): T
}
