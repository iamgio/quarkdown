package eu.iamgio.quarkdown.lexer.patterns

import eu.iamgio.quarkdown.lexer.regex.pattern.TokenRegexPattern
import eu.iamgio.quarkdown.lexer.tokens.TextSymbolToken

/**
 * Patterns for sequences of characters that correspond to text symbols.
 */
enum class TextSymbolReplacement(val result: String, val regex: Regex) {
    /**
     * `(C)` -> `©`
     */
    COPYRIGHT("©", "\\(C\\)".toRegex()),

    /**
     * `(R)` -> `®`
     */
    REGISTERED("®", "\\(R\\)".toRegex()),

    /**
     * `(TM)` -> `™`
     */
    TRADEMARK("™", "\\(TM\\)".toRegex()),

    /**
     * `-` -> `—`
     *
     * It must be surrounded by a word character and a space on both sides.
     */
    EM_DASH("—", "(?<=\\w\\s)-(?=\\s\\w)".toRegex()),

    /**
     * `...` -> `…`
     *
     * Must be either at the beginning or end of a word, not in-between.
     */
    ELLIPSIS("…", "(\\.\\.\\.(?=\\s|\$))|((?<=\\s|^)\\.\\.\\.)".toRegex()),

    /**
     * `->` -> `→`
     */
    SINGLE_RIGHT_ARROW("→", "->".toRegex()),

    /**
     * `<-` -> `←`
     */
    SINGLE_LEFT_ARROW("←", "<-".toRegex()),

    /**
     * `=>` -> `⇒`
     */
    DOUBLE_RIGHT_ARROW("⇒", "=>".toRegex()),

    /**
     * `<==` -> `⇐`
     */
    DOUBLE_LEFT_ARROW("⇐", "<==".toRegex()),

    /**
     * `>=` -> `≥`
     */
    GREATER_EQUAL("≥", ">=".toRegex()),

    /**
     * `<=` -> `≤`
     */
    LESS_EQUAL("≤", "<=".toRegex()),

    /**
     * `!=` -> `≠`
     */
    NOT_EQUAL("≠", "!=".toRegex()),

    /**
     * `+-` -> `±`
     */
    PLUS_MINUS("±", "\\+-".toRegex()),

    /**
     * `'` -> `’`
     *
     * Must be preceded by a word character.
     */
    TYPOGRAPHIC_APOSTROPHE("’", "(?<=\\w)'".toRegex()),
    ;

    /**
     * @return this replacement to a pattern that matches the symbol and wraps it in a [TextSymbolToken]
     */
    fun toTokenPattern() =
        TokenRegexPattern(
            name = "InlineTextReplacement${name.replace("_", "")}",
            wrap = { data -> TextSymbolToken(data, symbol = this) },
            regex = regex,
        )
}
