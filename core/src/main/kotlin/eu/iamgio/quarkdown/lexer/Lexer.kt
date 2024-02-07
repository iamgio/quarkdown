package eu.iamgio.quarkdown.lexer

/**
 * A scanner that transforms raw string data into a list of token.
 * For instance, the Markdown code `Hello _Quarkdown_` is tokenized into `Hello `, `_`, `Quarkdown`, `_`.
 * See [lexing_examples.txt](https://github.com/iamgio/quarkdown/blob/analysis/lexing_examples.txt) for further information.
 * @param source the content to tokenize
 */
class Lexer(private val source: CharSequence) {
    // Index of the last scanned character within the source code.
    private var lastCharIndex = 0

    /**
     * Creates a text [Token] in the range [lastCharIndex]-[untilIndex].
     * Text tokens do not have a [Regex] pattern, thus have no capture groups.
     * @param untilIndex final text range delimiter (exclusive)
     * @return a new text token
     */
    private fun createTextToken(untilIndex: Int): Token {
        val range = lastCharIndex until untilIndex
        val text = source.substring(range)

        // TODO literal value could also be number or boolean (need to parse here)

        return Token(
            TokenType.TEXT,
            text,
            text,
            position = range,
        )
    }

    /**
     * Converts captured groups of a [Regex] match to a sequence of tokens.
     * Uncaptured parts of the source string are converted into text tokens.
     * @param result result of the [Regex] match
     * @return stream of matched tokens
     */
    private fun extractMatchingTokens(result: MatchResult): List<Token> =
        buildList {
            TokenTypePattern.values().forEach { pattern ->
                val group = result.groups[pattern.name] ?: return@forEach
                val range = group.range

                // The token itself.
                val token =
                    Token(
                        type = pattern.tokenType,
                        text = group.value,
                        // TODO
                        literal = null,
                        position = range,
                    )

                // Text tokens are substrings that were not captured by any pattern.
                // These uncaptured groups are scanned and converted to tokens.
                if (range.first > lastCharIndex) {
                    this += createTextToken(untilIndex = range.first)
                }

                this += token

                lastCharIndex = range.last + 1
            }
        }

    /**
     * Disassembles some raw string into smaller tokens.
     * @return the ordered list of tokens
     */
    fun tokenize(): List<Token> =
        buildList {
            lastCharIndex = 0

            val regex = TokenTypePattern.groupify()
            val match = regex.findAll(source)

            match.forEach { result ->
                addAll(extractMatchingTokens(result))
            }
        }
}
