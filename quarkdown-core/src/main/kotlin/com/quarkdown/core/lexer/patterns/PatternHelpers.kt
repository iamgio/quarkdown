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

    /** Comments */
    val COMMENT = "<!--(-?>|[\\s\\S]*?-->)".toRegex()

    /**
     * Custom ID: {#custom-id}
     * @param prefixName the prefix for the named capturing group, preceding `customid`.
     *                   This is needed in order to avoid name clashes when combining multiple patterns.
     */
    fun customId(prefixName: String) = "(?:[ \\t]*\\{#(?<${prefixName}customid>[^}]+)})"
}
