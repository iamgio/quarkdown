package eu.iamgio.quarkdown.lexer

import eu.iamgio.quarkdown.util.filterValuesNotNull

/**
 * A scanner that transforms raw string data into a list of token.
 * For instance, the Markdown code `Hello _Quarkdown_` is tokenized into `Hello `, `_`, `Quarkdown`, `_`.
 * See [lexing_examples.txt](https://github.com/iamgio/quarkdown/blob/analysis/lexing_examples.txt) for further information.
 * @param source the content to tokenize
 */
class Lexer(private val source: CharSequence) {
    /**
     * Converts captured groups of a [Regex] match to a sequence of tokens.
     * @param result result of the [Regex] match
     * @return stream of matched tokens
     */
    private fun extractMatchingTokens(result: MatchResult): Sequence<Token> =
        TokenTypePattern.values().asSequence()
            .map { it to result.groups[it.name] }
            .filterValuesNotNull()
            .map { (pattern, group) ->
                Token(
                    type = pattern.tokenType,
                    text = group.value,
                    // TODO
                    literal = null,
                    position = group.range,
                )
            }

    /**
     * Disassembles some raw string into smaller tokens.
     * @return the ordered list of tokens
     */
    fun tokenize(): List<Token> =
        buildList {
            // TODO must also tokenize regular text (= not in a match group)

            val regex = TokenTypePattern.groupify()
            val match = regex.findAll(source)

            match.forEach { result ->
                addAll(extractMatchingTokens(result))
            }
        }
}
