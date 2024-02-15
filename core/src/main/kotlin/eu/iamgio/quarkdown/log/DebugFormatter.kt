package eu.iamgio.quarkdown.log

import eu.iamgio.quarkdown.lexer.TokenDecorator

/**
 * Utilities to log prettier debugging data.
 */
object DebugFormatter {
    /**
     * Pretty-formats a list of tokens.
     * @param tokens tokens to format
     * @return formatted string
     */
    fun formatTokens(tokens: Iterable<TokenDecorator>): String {
        val format = "%-25s %-20s %s" // Columns

        return tokens.joinToString(separator = "\n") { token ->
            val type = "type: ${token.javaClass.simpleName.removeSuffix("Token")}"
            val pos = "pos: ${token.data.position}"

            val content =
                token.data.text
                    .replace("\\R".toRegex(), "\\\\n")
                    .replace("\t", "\\t")
                    .replace("    ", "\\t")

            val text = "text: $content"

            format.format(type, pos, text)
        }
    }
}
