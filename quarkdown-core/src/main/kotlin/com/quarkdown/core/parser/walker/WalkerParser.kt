package com.quarkdown.core.parser.walker

import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.parser.toParsedOrThrow

/**
 * Lexer and parser that, thanks to `better-parse`'s context-free [Grammar], can parse a string into a structured object.
 * The term `Walker` refers to the fact this might be invoked by Quarkdown's lexer to walk through the source string,
 * in case content cannot be tokenized via regular expressions.
 * @param T the type of the parsed object
 * @param source the content to be parsed
 * @param grammar the grammar that defines the parsing rules
 * @see WalkerParsingResult
 */
open class WalkerParser<T>(
    private val source: CharSequence,
    private val grammar: Grammar<T>,
) {
    /**
     * Parses the [source] string into an output object according to the [grammar]-defined rules.
     * The parser interrupts when it reaches the end of the source string or when it encounters a syntax error.
     * @return the result of the parsing operation
     */
    fun parse(): WalkerParsingResult<T> {
        val tokens = grammar.tokenizer.tokenize(source.toString())
        val result = grammar.tryParse(tokens, fromPosition = 0)
        val parsed = result.toParsedOrThrow()

        val endIndex = tokens[parsed.nextPosition]?.offset ?: source.length
        val remainder = source.substring(endIndex)
        return WalkerParsingResult(parsed.value, endIndex, tokens, remainder)
    }
}
