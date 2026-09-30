package com.quarkdown.core.lexer.scan

private val PUNCTUATION_CATEGORIES =
    setOf(
        CharCategory.CONNECTOR_PUNCTUATION,
        CharCategory.DASH_PUNCTUATION,
        CharCategory.START_PUNCTUATION,
        CharCategory.END_PUNCTUATION,
        CharCategory.INITIAL_QUOTE_PUNCTUATION,
        CharCategory.FINAL_QUOTE_PUNCTUATION,
        CharCategory.OTHER_PUNCTUATION,
        CharCategory.MATH_SYMBOL,
        CharCategory.CURRENCY_SYMBOL,
        CharCategory.MODIFIER_SYMBOL,
        CharCategory.OTHER_SYMBOL,
    )

/**
 * Unicode character classification, and the narrower ASCII whitespace class the syntax rules are defined in.
 */
object CharCategories {
    /**
     * @param char character to classify
     * @return whether the character belongs to a Unicode punctuation or symbol category
     */
    fun isPunctuationOrSymbol(char: Char): Boolean = char.category in PUNCTUATION_CATEGORIES

    /**
     * The whitespace every block and inline rule is defined in: ASCII only, so a no-break space is ordinary
     * text and does not separate a heading's hashes from its title. Emphasis flanking is the one rule keyed
     * on Unicode whitespace, as CommonMark 0.31.2 defines it.
     *
     * @param char character to classify
     * @return whether the character is one of space, tab, line feed, vertical tab, form feed or carriage return
     */
    fun isAsciiWhitespace(char: Char): Boolean =
        char == ' ' || char == '\t' || char == '\n' || char == '\u000b' || char == '\u000c' || char == '\r'

    /**
     * @param char character to classify
     * @return whether the character is an ASCII letter or digit
     */
    fun isAsciiLetterOrDigit(char: Char): Boolean = char in 'a'..'z' || char in 'A'..'Z' || char in '0'..'9'

    /**
     * @param char character to classify
     * @return whether the character is an ASCII letter, digit or underscore
     */
    fun isAsciiWordChar(char: Char): Boolean = isAsciiLetterOrDigit(char) || char == '_'
}

/**
 * The highest code point that is not printable: no destination or autolink may hold one.
 */
internal const val LOWEST_PRINTABLE = 0x1f
