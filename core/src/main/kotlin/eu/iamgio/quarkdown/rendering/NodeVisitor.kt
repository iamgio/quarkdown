package eu.iamgio.quarkdown.rendering

import eu.iamgio.quarkdown.ast.AstRoot
import eu.iamgio.quarkdown.ast.PlainText
import eu.iamgio.quarkdown.ast.Strong

/**
 * A visitor for [eu.iamgio.quarkdown.ast.Node]s.
 * @param T output type of the `visit` methods
 */
interface NodeVisitor<T> {
    fun visit(node: AstRoot): T

    // Block

    // Inline

    fun visit(node: PlainText): T

    fun visit(node: Strong): T
}
