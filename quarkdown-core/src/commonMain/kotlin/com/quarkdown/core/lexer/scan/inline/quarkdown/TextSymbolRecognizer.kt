package com.quarkdown.core.lexer.scan.inline.quarkdown

import com.quarkdown.core.lexer.scan.inline.InlineMatch
import com.quarkdown.core.lexer.scan.inline.InlineRecognizer
import com.quarkdown.core.lexer.scan.inline.inlineMatch
import com.quarkdown.core.lexer.tokens.TextSymbolToken

/**
 * Recognizes a sequence of characters that stands for a symbol.
 * @param replacement the symbol to look for
 */
class TextSymbolRecognizer(
    private val replacement: TextSymbolReplacement,
) : InlineRecognizer {
    override fun recognize(
        source: CharSequence,
        index: Int,
    ): InlineMatch? {
        val length = replacement.matches(source, index) ?: return null
        val end = index + length
        return inlineMatch(source, index, end) { TextSymbolToken(it, symbol = replacement) }
    }
}

/**
 * @return a recognizer that looks for this replacement's sequence
 */
fun TextSymbolReplacement.toRecognizer() = TextSymbolRecognizer(this)
