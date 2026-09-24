package com.quarkdown.core.lexer

/**
 * A [Lexer] that expects iterations through [source] content.
 * @param source the content to be tokenized
 */
abstract class AbstractLexer(
    override val source: CharSequence,
) : Lexer {
    /**
     * Index of the latest scanned character within [source].
     */
    abstract val currentIndex: Int

    /**
     * @param position range of the uncaptured group
     * @return a new token that represents the uncaptured content in order to fill the gaps, or `null` to not fill gaps
     */
    abstract fun createFillToken(position: IntRange): Token?
}
