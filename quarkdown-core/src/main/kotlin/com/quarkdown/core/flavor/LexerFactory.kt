package com.quarkdown.core.flavor

import com.quarkdown.core.lexer.Lexer

/**
 * Provider of [Lexer] instances. Each factory method returns a specialized implementation for a specific kind of tokenization.
 */
interface LexerFactory {
    /**
     * @param source raw input
     * @return a new [Lexer] instance that tokenizes macro blocks
     */
    fun newBlockLexer(source: CharSequence): Lexer

    /**
     * @param source raw input
     * @return a new [Lexer] instance that tokenizes list items
     */
    fun newListLexer(source: CharSequence): Lexer

    /**
     * @param source raw input
     * @param variant the variant of inline lexer to create, affecting which tokens are recognized.
     * For example, link labels don't recognize link tokens,
     * so the [InlineLexerVariant.LINK_LABEL] variant can be used
     * @return a new [Lexer] instance that tokenizes inline content
     */
    fun newInlineLexer(
        source: CharSequence,
        variant: InlineLexerVariant = InlineLexerVariant.NORMAL,
    ): Lexer

    /**
     * @param source raw input
     * @param allowBlockFunctionCalls whether block function calls are tokenized too
     * @return a new [Lexer] instance that distinguishes text (static values) from function calls
     */
    fun newExpressionLexer(
        source: CharSequence,
        allowBlockFunctionCalls: Boolean,
    ): Lexer
}

/**
 * Variants of inline lexers.
 */
enum class InlineLexerVariant {
    NORMAL,
    LINK_LABEL,
}
