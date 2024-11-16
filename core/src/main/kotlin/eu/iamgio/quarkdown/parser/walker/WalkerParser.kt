package eu.iamgio.quarkdown.parser.walker

import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.parser.toParsedOrThrow

/**
 *
 */
open class WalkerParser<T>(private val source: CharSequence, private val grammar: Grammar<T>) {
    fun parse(): WalkerParsingResult<T> {
        val tokens = grammar.tokenizer.tokenize(source.toString())
        val result = grammar.tryParse(tokens, fromPosition = 0)
        val parsed = result.toParsedOrThrow()

        val endIndex = tokens[parsed.nextPosition]?.offset ?: source.length
        return WalkerParsingResult(parsed.value, endIndex)
    }
}
