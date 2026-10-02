package com.quarkdown.core.lexer

import com.quarkdown.core.util.ConcurrentCache

/**
 * Memoizes tokens by source, so that a source tokenized repeatedly,
 * such as the body of a lambda invoked in a loop, is lexed only once.
 *
 * Lexing is a pure function of the source, hence tokens are safe to share between parses.
 * Since tokens are memoized by source alone, a cache must always be used with the same kind of lexer.
 *
 * Thread-safe.
 */
class TokenCache {
    private val tokens = ConcurrentCache<String, List<Token>>()

    /**
     * @param source source to tokenize
     * @param newLexer creates the lexer of [source], invoked only if [source] was not lexed before
     * @return the tokens of [source]
     */
    fun getOrLex(
        source: String,
        newLexer: (String) -> Lexer,
    ): Sequence<Token> = tokens.getOrPut(source) { newLexer(source).tokenize().toList() }.asSequence()
}
