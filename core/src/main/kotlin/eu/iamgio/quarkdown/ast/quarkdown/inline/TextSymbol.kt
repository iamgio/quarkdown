package eu.iamgio.quarkdown.ast.quarkdown.inline

import eu.iamgio.quarkdown.ast.base.inline.PlainTextNode
import eu.iamgio.quarkdown.visitor.node.NodeVisitor

/**
 * A text-based symbol, such as `©`, `…`, `≥`.
 * This is usually the result of a combination of multiple characters (e.g. `(C)` -> `©`).
 * @param symbol processed symbol (e.g. `©`)
 * @see eu.iamgio.quarkdown.lexer.patterns.TextSymbolReplacement
 */
data class TextSymbol(val symbol: Char) : PlainTextNode {
    /**
     * @return [symbol] as a string
     */
    override val text: String
        get() = symbol.toString()

    override fun <T> accept(visitor: NodeVisitor<T>): T = visitor.visit(this)
}
