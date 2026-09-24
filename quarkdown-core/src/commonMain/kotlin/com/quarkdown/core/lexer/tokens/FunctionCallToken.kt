package com.quarkdown.core.lexer.tokens

import com.quarkdown.core.lexer.Token
import com.quarkdown.core.lexer.TokenData
import com.quarkdown.core.parser.walker.WalkerParsingResult
import com.quarkdown.core.parser.walker.funcall.WalkedFunctionCall
import com.quarkdown.core.visitor.token.TokenVisitor

/**
 * A function call token, produced by the lexer's walker subsystem.
 * This is a custom Quarkdown element, and is both a block and inline node.
 *
 * Example:
 * ```
 * .function {arg1} {arg2}
 *     body
 * ```
 * The `body` argument is supported only when used as a block.
 * @param isBlock whether the function call is a block (opposite: inline)
 * @param walkerResult the result of the walker parsing, containing the structured [WalkedFunctionCall]
 * @see com.quarkdown.core.ast.quarkdown.FunctionCallNode
 */
class FunctionCallToken(
    data: TokenData,
    val isBlock: Boolean,
    val walkerResult: WalkerParsingResult<WalkedFunctionCall>,
) : Token(data) {
    override fun <T> accept(visitor: TokenVisitor<T>) = visitor.visit(this)
}
