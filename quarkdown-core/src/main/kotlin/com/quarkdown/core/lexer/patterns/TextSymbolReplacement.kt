package com.quarkdown.core.lexer.patterns

import com.quarkdown.core.lexer.regex.pattern.TokenRegexPattern
import com.quarkdown.core.lexer.tokens.TextSymbolToken

/**
 * Patterns for sequences of characters that correspond to text symbols.
 * @param result symbol that the sequence is replaced with
 * @param regex regex pattern that matches the sequence to be replaced
 */
enum class TextSymbolReplacement(
    val result: Char,
    val regex: Regex,
) {
    /**
     * `(C)` -> `©`
     */
    COPYRIGHT('©', "\\(C\\)".toRegex()),

    /**
     * `(R)` -> `®`
     */
    REGISTERED('®', "\\(R\\)".toRegex()),

    /**
     * `(TM)` -> `™`
     */
    TRADEMARK('™', "\\(TM\\)".toRegex()),

    /**
     * `--` -> `—`
     */
    EM_DASH('—', "--".toRegex()),

    /**
     * `-` -> `–`
     *
     * It must be surrounded by a word character and a space on both sides.
     */
    EN_DASH('–', "(?<=\\w\\s)-(?=\\s\\w)".toRegex()),

    /**
     * `...` -> `…`
     *
     * Must be either at the beginning or end of a word, not in-between.
     */
    ELLIPSIS('…', "(\\.\\.\\.(?=\\s|\$))|((?<=\\s|^)\\.\\.\\.)".toRegex()),

    /**
     * `->` -> `→`
     */
    SINGLE_RIGHT_ARROW('→', "->".toRegex()),

    /**
     * `<-` -> `←`
     */
    SINGLE_LEFT_ARROW('←', "<-".toRegex()),

    /**
     * `=>` -> `⇒`
     */
    DOUBLE_RIGHT_ARROW('⇒', "=>".toRegex()),

    /**
     * `<==` -> `⇐`
     */
    DOUBLE_LEFT_ARROW('⇐', "<==".toRegex()),

    /**
     * `>=` -> `≥`
     */
    GREATER_EQUAL('≥', ">=".toRegex()),

    /**
     * `<=` -> `≤`
     */
    LESS_EQUAL('≤', "<=".toRegex()),

    /**
     * `!=` -> `≠`
     */
    NOT_EQUAL('≠', "!=".toRegex()),

    /**
     * `+-` -> `±`
     */
    PLUS_MINUS('±', "\\+-".toRegex()),

    /**
     * `'` -> `‘`
     *
     * Must not be preceded by a word and must be followed by a word character.
     */
    TYPOGRAPHIC_LEFT_APOSTROPHE('‘', "(?<=\\s|^)'(?=\\w)".toRegex()),

    /**
     * `'` -> `’`
     *
     * Must not be preceded by a whitespace.
     */
    TYPOGRAPHIC_RIGHT_APOSTROPHE('’', "(?<!\\s)'".toRegex()),

    /**
     * `"` -> `“`
     *
     * Must not be preceded by a word character and must be followed by a word character.
     */
    TYPOGRAPHIC_LEFT_QUOTATION_MARK('“', "(?<!\\w)\"(?=\\w)".toRegex()),

    /**
     * `"` -> `”`
     *
     * Must not be preceded by a whitespace and not followed by a word character.
     */
    TYPOGRAPHIC_RIGHT_QUOTATION_MARK('”', "(?<!\\s)\"(?!\\w)".toRegex()),
    ;

    /**
     * @return this replacement to a pattern that matches the symbol and wraps it in a [TextSymbolToken]
     */
    fun toTokenPattern() =
        TokenRegexPattern(
            name = "InlineTextReplacement${name.replace("_", "")}",
            wrap = { data -> TextSymbolToken(data, symbol = this) },
            regex = regex.pattern,
        )
}
