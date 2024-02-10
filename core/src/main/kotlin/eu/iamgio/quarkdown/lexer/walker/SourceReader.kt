package eu.iamgio.quarkdown.lexer.walker

import java.io.Reader

/**
 * A char-by-char string reader.
 * @param reader internal reader
 */
class SourceReader(private val reader: Reader) {
    /**
     * The current character index the reader is at.
     */
    var index = 0
        private set

    /**
     * @param source source string to generate the reader for
     */
    constructor(source: CharSequence) : this(source.toString().reader())

    /**
     * @return the value at the current index, if there is any
     */
    fun peek(): Char? {
        reader.mark(1)
        return readWithoutIncrement().also {
            reader.reset()
        }
    }

    private fun readWithoutIncrement(): Char? {
        return reader.read().takeUnless { it == -1 }?.toChar()
    }

    /**
     * Reads the current character and moves the index forward.
     * @return the value at the current index, if there is any
     */
    fun read(): Char? {
        return readWithoutIncrement()?.also { index++ }
    }
}
