package eu.iamgio.quarkdown.lexer

/**
 * A scanner that transforms raw string data into a list of token.
 * For instance, the Markdown code `Hello _Quarkdown_` is tokenized into `Hello `, `_`, `Quarkdown`, `_`.
 * See [lexing_examples.txt](https://github.com/iamgio/quarkdown/blob/analysis/lexing_examples.txt) for further information.
 * @param source the content to tokenize
 */
class Lexer(private val source: CharSequence) {
    /**
     * Disassembles some raw string into smaller tokens.
     * @return the ordered list of tokens
     */
    fun tokenize(): List<Token> {
        val tokens = mutableListOf<Token>()

        return tokens
    }
}
