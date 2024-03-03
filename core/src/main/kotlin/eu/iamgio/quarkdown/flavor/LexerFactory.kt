package eu.iamgio.quarkdown.flavor

import eu.iamgio.quarkdown.lexer.Lexer

/**
 *
 */
interface LexerFactory {
    fun newBlockLexer(source: CharSequence): Lexer

    fun newListLexer(source: CharSequence): Lexer

    fun newInlineLexer(source: CharSequence): Lexer
}
