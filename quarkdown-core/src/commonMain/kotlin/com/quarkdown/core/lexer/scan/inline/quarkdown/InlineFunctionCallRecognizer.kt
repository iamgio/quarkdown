package com.quarkdown.core.lexer.scan.inline.quarkdown

import com.quarkdown.core.lexer.TokenData
import com.quarkdown.core.lexer.scan.functionCallStartAt
import com.quarkdown.core.lexer.scan.inline.InlineMatch
import com.quarkdown.core.lexer.scan.inline.InlineRecognizer
import com.quarkdown.core.lexer.scan.isFunctionCallAllowedBefore
import com.quarkdown.core.lexer.scan.walkFunctionCallAt
import com.quarkdown.core.lexer.tokens.FunctionCallToken
import com.quarkdown.core.parser.walker.funcall.FunctionCallGrammar

/**
 * Recognizes an inline function call.
 * The produced token is zero-width: the call's extent comes from the walker and reaches the parser through
 * the walker result.
 */
object InlineFunctionCallRecognizer : InlineRecognizer {
    override fun recognize(
        source: CharSequence,
        index: Int,
    ): InlineMatch? {
        if (!functionCallStartAt(source, index)) return null
        // Only the `.name` form constrains what precedes it; a wrapped call may follow any character.
        val isWrapped = source.getOrNull(index) == FunctionCallGrammar.ARGUMENT_BEGIN
        if (!isWrapped && !isFunctionCallAllowedBefore(source, index)) return null
        val result = walkFunctionCallAt(source, index) ?: return null
        val token = FunctionCallToken(TokenData(text = "", position = index until index), isBlock = false, walkerResult = result)
        return InlineMatch(token, index + result.endIndex)
    }
}
