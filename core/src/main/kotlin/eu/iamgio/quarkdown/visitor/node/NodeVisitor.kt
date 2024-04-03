package eu.iamgio.quarkdown.visitor.node

import eu.iamgio.quarkdown.ast.AstRoot
import eu.iamgio.quarkdown.ast.BaseListItem
import eu.iamgio.quarkdown.ast.BlockQuote
import eu.iamgio.quarkdown.ast.BlockText
import eu.iamgio.quarkdown.ast.Code
import eu.iamgio.quarkdown.ast.CodeSpan
import eu.iamgio.quarkdown.ast.Comment
import eu.iamgio.quarkdown.ast.CriticalContent
import eu.iamgio.quarkdown.ast.Emphasis
import eu.iamgio.quarkdown.ast.Heading
import eu.iamgio.quarkdown.ast.HorizontalRule
import eu.iamgio.quarkdown.ast.Html
import eu.iamgio.quarkdown.ast.Image
import eu.iamgio.quarkdown.ast.LineBreak
import eu.iamgio.quarkdown.ast.Link
import eu.iamgio.quarkdown.ast.LinkDefinition
import eu.iamgio.quarkdown.ast.Math
import eu.iamgio.quarkdown.ast.MathSpan
import eu.iamgio.quarkdown.ast.Newline
import eu.iamgio.quarkdown.ast.OrderedList
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

/**
 * A visitor for [eu.iamgio.quarkdown.ast.Node]s.
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

    fun visit(node: OrderedList): T

    fun visit(node: UnorderedList): T

    fun visit(node: BaseListItem): T

    fun visit(node: TaskListItem): T

    fun visit(node: Html): T

    fun visit(node: Table): T

    fun visit(node: Paragraph): T

    fun visit(node: BlockQuote): T

    fun visit(node: BlockText): T

    // Base inline

    fun visit(node: Comment): T

    fun visit(node: LineBreak): T

    fun visit(node: CriticalContent): T

    fun visit(node: Link): T

    fun visit(node: ReferenceLink): T

    fun visit(node: Image): T

    fun visit(node: ReferenceImage): T

    fun visit(node: Text): T

    fun visit(node: CodeSpan): T

    fun visit(node: Emphasis): T

    fun visit(node: Strong): T

    fun visit(node: StrongEmphasis): T

    fun visit(node: Strikethrough): T

    // Quarkdown block

    fun visit(node: Math): T

    // Quarkdown inline
    fun visit(node: MathSpan): T
}
