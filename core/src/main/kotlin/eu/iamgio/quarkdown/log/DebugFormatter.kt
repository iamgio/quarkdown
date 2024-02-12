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
    fun formatTokens(tokens: Iterable<Token>): String {
        val format = "%-30s %-20s %s" // Columns

        return tokens.joinToString(separator = "\n") { token ->
            val type = "type: ${token.type}"
            val pos = "pos: ${token.position}"
            val text = "text: " + token.text
                .replace("\\R".toRegex(), "\\\\n")
                .replace("\t", "\\t")
                .replace("    ", "\\t")

            format.format(type, pos, text)
        }
    }
}
