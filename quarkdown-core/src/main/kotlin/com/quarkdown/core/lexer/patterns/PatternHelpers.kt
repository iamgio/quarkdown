package com.quarkdown.core.lexer.patterns

/**
 * Helper patterns for regex lexers.
 */
internal object PatternHelpers {
    /** Bullet point for unordered and ordered lists. */
    const val BULLET = "[*+-]|\\d{1,9}[\\.)]"

    /**
     * Title enclosed in delimiters.
     * - `"This is a title"`
     * - `'This is a title'`
     * - `(This is a title)`
     */
    const val DELIMITED_TITLE = """"(?:\\.|[^"\\])*"|'(?:\\.|[^'\\])*'|\((?:\\.|[^)\\])*\)"""

    /**
     * Pattern of one-line fenced content between two dollar signs,
     * which is used for [com.quarkdown.core.lexer.tokens.OnelineMathToken] and [com.quarkdown.core.lexer.tokens.InlineMathToken]
     * The spacing between the dollar signs and the inner content must be of one unit.
     *
     * Inner dollar signs are included in the content as long as they are not adjacent to whitespace or non-word characters.
     */
    const val ONELINE_MATH =
        // Starting delimiter.
        "\\$[ \\t]" +
            // Ungreedy content: stop at the first delimiter wrapped by whitespace.
            "((?:[^$\\n]|[^\\s]\\$|\\$[^\\s])+?)" +
            // Ending delimiter.
            "(?<![ \\t])[ \\t]\\$"

    /** Comments. */
    val COMMENT = "<!--(-?>|[\\s\\S]*?-->)".toRegex()

    /**
     * Custom ID: {#custom-id}
     * @param prefixName the prefix for the named capturing group, preceding `customid`.
     *                   This is needed in order to avoid name clashes when combining multiple patterns.
     */
    fun customId(prefixName: String) = "(?:[ \\t]*\\{#(?<${prefixName}customid>[^}]+)})"
}
