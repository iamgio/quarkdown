package eu.iamgio.quarkdown.parser.visitor

import eu.iamgio.quarkdown.lexer.PlainTextToken

/**
 * A visitor for inline [eu.iamgio.quarkdown.lexer.Token]s.
 * @param T output type of the `visit` methods
 */
interface InlineTokenVisitor<T> {
    fun visit(token: PlainTextToken): T
}
