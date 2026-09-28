package com.quarkdown.core.lexer.scan.inline.quarkdown

import com.quarkdown.core.lexer.TokenData
import com.quarkdown.core.lexer.scan.MAX_BLOCK_INDENT
import com.quarkdown.core.lexer.scan.Scanner
import com.quarkdown.core.lexer.scan.functionCallStartAt
import com.quarkdown.core.lexer.scan.inline.DelimiterRuns
import com.quarkdown.core.lexer.scan.inline.InlineMatch
import com.quarkdown.core.lexer.scan.inline.InlineRecognizer
import com.quarkdown.core.lexer.scan.walkFunctionCallAt
import com.quarkdown.core.lexer.tokens.FunctionCallToken

/**
 * Recognizes a function call at the start of a line while evaluating an expression.
 *
 * Trailing content on the call's last line never disqualifies it, because an expression draws no distinction
 * between a block and an inline call. Its token covers the line's leading spaces, so it is zero-width only on
 * an unindented line.
 */
object ExpressionFunctionCallRecognizer : InlineRecognizer {
    override fun recognize(
        source: CharSequence,
        index: Int,
    ): InlineMatch? {
        if (!DelimiterRuns.isLineStart(source, index)) return null
        val scanner = Scanner(source, index = index)
        repeat(MAX_BLOCK_INDENT) { scanner.take(' ') }
        val flagStart = scanner.index
        if (!functionCallStartAt(source, flagStart)) return null
        val result = walkFunctionCallAt(source, flagStart) ?: return null
        val data = TokenData(source.substring(index, flagStart), index until flagStart)
        return InlineMatch(FunctionCallToken(data, isBlock = true, walkerResult = result), flagStart + result.endIndex)
    }
}
