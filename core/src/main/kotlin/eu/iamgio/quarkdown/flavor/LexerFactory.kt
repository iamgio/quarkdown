package eu.iamgio.quarkdown.flavor

import eu.iamgio.quarkdown.lexer.Lexer

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
     * @return a new [Lexer] instance that tokenizes inline content
     */
    fun newInlineLexer(source: CharSequence): Lexer

    /**
     * @param source raw input
     * @return a new [Lexer] instance that tokenizes inline content within a link label
     *         (e.g. nested links are not allowed)
     */
    fun newLinkLabelInlineLexer(source: CharSequence): Lexer
}
