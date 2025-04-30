package com.quarkdown.core.lexer.tokens

import com.quarkdown.core.lexer.Token
import com.quarkdown.core.lexer.TokenData
import com.quarkdown.core.visitor.token.TokenVisitor

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
 * @param isBlock whether the function call is a block (opposite: inline)
 * @see com.quarkdown.core.ast.FunctionCallNode
 */
class FunctionCallToken(data: TokenData, val isBlock: Boolean) : Token(data) {
    override fun <T> accept(visitor: TokenVisitor<T>) = visitor.visit(this)
}
