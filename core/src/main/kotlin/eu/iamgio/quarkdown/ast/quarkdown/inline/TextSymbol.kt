package eu.iamgio.quarkdown.ast.quarkdown.inline

import eu.iamgio.quarkdown.ast.base.inline.PlainTextNode
import eu.iamgio.quarkdown.visitor.node.NodeVisitor

/**
 * A text-based symbol, such as `©`, `…`, `≥`.
 * This is usually the result of a combination of multiple characters (e.g. `(C)` -> `©`).
 * @param text processed symbol (e.g. `©`)
 * @see eu.iamgio.quarkdown.lexer.patterns.TextSymbolReplacement
 */
data class TextSymbol(override val text: String) : PlainTextNode {
    override fun <T> accept(visitor: NodeVisitor<T>): T = visitor.visit(this)
}
