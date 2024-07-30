package eu.iamgio.quarkdown.lexer.walker

import eu.iamgio.quarkdown.lexer.AbstractLexer
import eu.iamgio.quarkdown.lexer.Lexer
import eu.iamgio.quarkdown.lexer.TokenData

/**
 * A [Lexer] that scans the source character-by-character.
 * @param source the content to be tokenized
 * @param reader a char-by-char string reader
 */
abstract class WalkerLexer(source: CharSequence, protected val reader: SourceReader = SourceReader(source)) :
    AbstractLexer(source) {
    /**
     * A buffer that can be used to store scanned characters until they are converted into a token.
     */
    protected val buffer = StringBuilder()

    /**
     * Creates a token from the buffered data and clears the buffer.
     * @return a new instance of [TokenData] that contains [buffer]'s data
     */
    fun createTokenDataFromBuffer(): TokenData =
        TokenData(
            text = buffer.toString(),
            position = currentIndex - buffer.length until currentIndex,
        ).also { buffer.clear() }

    override val currentIndex: Int
        get() = reader.index
}
