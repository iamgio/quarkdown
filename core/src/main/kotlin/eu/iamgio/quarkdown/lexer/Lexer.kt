package eu.iamgio.quarkdown.lexer

/**
 * A scanner that transforms raw string data into a list of token.
 * For instance, the Markdown code `Hello _Quarkdown_` is tokenized by its implementation into `Hello `, `_`, `Quarkdown`, `_`.
 * See [lexing_examples.txt](https://github.com/iamgio/quarkdown/blob/analysis/lexing_examples.txt) for further information.
 */
interface Lexer {
    /**
     * The content to be tokenized.
     */
    val source: CharSequence

    /**
     * Disassembles some raw string into smaller tokens.
     * @return the ordered list of tokens
     */
    fun tokenize(): List<Token>

    /**
     * @param source the content to be tokenized by the new lexer
     * @return a new instance of this kind of lexer with [source] as its source
     */
    fun copyWith(source: CharSequence): Lexer
}
