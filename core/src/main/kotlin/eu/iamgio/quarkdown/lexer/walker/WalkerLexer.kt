package eu.iamgio.quarkdown.lexer.walker

import eu.iamgio.quarkdown.lexer.AbstractLexer
import eu.iamgio.quarkdown.lexer.Lexer

/**
 * A [Lexer] that scans the source character-by-character.
 * @param source the content to be tokenized
 * @param reader a char-by-char string reader
 */
abstract class WalkerLexer(source: CharSequence, protected val reader: SourceReader = SourceReader(source)) : AbstractLexer(source) {
    protected val buffer = StringBuilder()
}
