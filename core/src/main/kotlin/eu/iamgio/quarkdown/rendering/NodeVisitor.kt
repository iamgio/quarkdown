package eu.iamgio.quarkdown.rendering

import eu.iamgio.quarkdown.ast.PlainText

/**
 * A visitor for [eu.iamgio.quarkdown.ast.Node]s.
 * @param T output type of the `visit` methods
 */
interface NodeVisitor<T> {
    fun visit(node: PlainText): T
}
