package eu.iamgio.quarkdown.lexer.walker

import eu.iamgio.quarkdown.lexer.AbstractLexer
import eu.iamgio.quarkdown.lexer.Lexer

/**
 * A [Lexer] that scans the source character-by-character.
 * @param source the content to be tokenized
 */
abstract class WalkerLexer(source: CharSequence) : AbstractLexer(source) {
    /**
     * A char-by-char string reader.
     */
    protected val reader = SourceReader(source)
}
