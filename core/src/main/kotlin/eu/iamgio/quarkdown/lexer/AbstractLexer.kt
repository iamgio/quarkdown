package eu.iamgio.quarkdown.lexer

/**
 * A [Lexer] that expects iterations through [source] content.
 * @param source the content to be tokenized
 */
abstract class AbstractLexer(override val source: CharSequence) : Lexer {
    /**
     * Index of the latest scanned character within [source].
     */
    protected var currentIndex = 0
}
