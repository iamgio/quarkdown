package eu.iamgio.quarkdown.rendering

import eu.iamgio.quarkdown.ast.AstRoot
import eu.iamgio.quarkdown.ast.Code
import eu.iamgio.quarkdown.ast.CodeSpan
import eu.iamgio.quarkdown.ast.Comment
import eu.iamgio.quarkdown.ast.CriticalContent
import eu.iamgio.quarkdown.ast.Emphasis
import eu.iamgio.quarkdown.ast.Image
import eu.iamgio.quarkdown.ast.LineBreak
import eu.iamgio.quarkdown.ast.Link
import eu.iamgio.quarkdown.ast.LinkDefinition
import eu.iamgio.quarkdown.ast.Newline
import eu.iamgio.quarkdown.ast.Paragraph
import eu.iamgio.quarkdown.ast.ReferenceImage
import eu.iamgio.quarkdown.ast.ReferenceLink
import eu.iamgio.quarkdown.ast.Strikethrough
import eu.iamgio.quarkdown.ast.Strong
import eu.iamgio.quarkdown.ast.StrongEmphasis
import eu.iamgio.quarkdown.ast.Text

/**
 * A visitor for [eu.iamgio.quarkdown.ast.Node]s.
 * @param T output type of the `visit` methods
 */
interface NodeVisitor<T> {
    fun visit(node: AstRoot): T

    // Block

    fun visit(node: Newline): T

    fun visit(node: Code): T

    fun visit(node: LinkDefinition): T

    fun visit(node: Paragraph): T

    // Inline

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
}
