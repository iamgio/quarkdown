package eu.iamgio.quarkdown.log

import eu.iamgio.quarkdown.lexer.Token

/**
 * Utilities to log prettier debugging data.
 */
object DebugFormatter {
    /**
     * Pretty-formats a list of tokens.
     * @param tokens tokens to format
     * @return formatted string
     */
    fun formatTokens(tokens: Iterable<Token>) =
        tokens.joinToString(separator = "\n") { token ->
            buildString {
                append("type: ")
                append(token.type)
                append("\t\t")

                append("pos: ")
                append(token.position)
                append("\t\t")

                append("text: ")

                append(
                    token.text
                        .replace("\\R".toRegex(), "\\\\n")
                        .replace("\t", "\\t")
                        .replace("    ", "\\t"),
                )
            }
        }
}
