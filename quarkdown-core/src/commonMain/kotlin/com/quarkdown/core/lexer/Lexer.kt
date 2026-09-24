package com.quarkdown.core.lexer

/**
 * A scanner that transforms raw string data into a list of token.
 * For instance, the Markdown code `Hello _Quarkdown_` is tokenized by its implementation into `Hello `, `_Quarkdown_`.
 */
interface Lexer {
    /**
     * The content to be tokenized.
     */
    val source: CharSequence

    /**
     * Disassembles some raw string into smaller tokens.
     * @return a lazy sequence of tokens, produced on demand
     */
    fun tokenize(): Sequence<Token>
}
