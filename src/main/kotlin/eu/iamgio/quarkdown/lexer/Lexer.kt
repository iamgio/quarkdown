package eu.iamgio.quarkdown.lexer

/**
 * A scanner that transforms raw string data into a list of token.
 * For instance, the Markdown code `Hello _Quarkdown_` is tokenized into `Hello `, `_`, `Quarkdown`, `_`.
 * @param source the content to tokenize
 */
class Lexer(private val source: CharSequence) {
    fun tokenize(): List<Token> {
        val tokens = mutableListOf<Token>()

        return tokens
    }
}
