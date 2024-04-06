package eu.iamgio.quarkdown.lexer.tokens

import eu.iamgio.quarkdown.lexer.Token
import eu.iamgio.quarkdown.lexer.TokenData
import eu.iamgio.quarkdown.visitor.token.TokenVisitor

/**
 * An inline function
 * This is a custom Quarkdown element, and is both a block and inline node.
 *
 * Example:
 * ```
 * .function {arg1} {arg2}
 *     body
 * ```
 * The `body` argument is supported only when used as a block.
 * @see eu.iamgio.quarkdown.ast.FunctionCallNode
 */
class FunctionCallToken(data: TokenData) : Token(data) {
    override fun <T> accept(visitor: TokenVisitor<T>) = visitor.visit(this)
}
