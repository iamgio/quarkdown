package eu.iamgio.quarkdown.flavor.base

import eu.iamgio.quarkdown.flavor.LexerFactory
import eu.iamgio.quarkdown.lexer.Lexer
import eu.iamgio.quarkdown.lexer.impl.BlockLexer

/**
 *
 */
class BaseMarkdownLexerFactory : LexerFactory {
    override fun newBlockLexer(source: CharSequence): Lexer = BlockLexer(source)

    override fun newInlineLexer(source: CharSequence): Lexer {
        TODO("Not yet implemented")
    }
}
