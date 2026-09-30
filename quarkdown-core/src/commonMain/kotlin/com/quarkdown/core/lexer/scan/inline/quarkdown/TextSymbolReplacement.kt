package com.quarkdown.core.lexer.scan.inline.quarkdown

import com.quarkdown.core.lexer.scan.CharCategories

/**
 * Sequences of characters that correspond to text symbols.
 *
 * The declaration order is the priority order: the em dash is declared before the en dash and the left
 * apostrophe before the right one, and swapping either pair changes the output.
 *
 * An entry whose sequence is a plain literal declares it here; the ones whose match depends on what surrounds
 * them override [matches].
 *
 * @param result symbol that the sequence is replaced with
 * @param sequence literal the entry matches, or `null` when it decides for itself
 * @param ignoreCase whether the literal matches in either case
 */
enum class TextSymbolReplacement(
    val result: Char,
    private val sequence: String? = null,
    private val ignoreCase: Boolean = false,
) {
    /**
     * `(C)` -> `©`
     */
    COPYRIGHT('©', "(C)", ignoreCase = true),

    /**
     * `(R)` -> `®`
     */
    REGISTERED('®', "(R)", ignoreCase = true),

    /**
     * `(TM)` -> `™`
     */
    TRADEMARK('™', "(TM)", ignoreCase = true),

    /**
     * `--` -> `—`
     */
    EM_DASH('—', "--"),

    /**
     * `-` -> `–`
     *
     * It must be surrounded by a word character and a space on both sides.
     */
    EN_DASH('–') {
        override fun matches(
            source: CharSequence,
            index: Int,
        ): Int? {
            if (source.getOrNull(index) != '-') return null
            val before = source.getOrNull(index - 2).isWordChar() && source.getOrNull(index - 1).isSpace()
            val after = source.getOrNull(index + 1).isSpace() && source.getOrNull(index + 2).isWordChar()
            return 1.takeIf { before && after }
        }
    },

    /**
     * `...` -> `…`
     *
     * Must be either at the beginning or end of a word, not in-between.
     */
    ELLIPSIS('…') {
        override fun matches(
            source: CharSequence,
            index: Int,
        ): Int? {
            if (literalAt(source, index, "...") == null) return null
            val endsWord = source.getOrNull(index + 3).isSpaceOrEdge()
            val startsWord = source.getOrNull(index - 1).isSpaceOrEdge()
            return 3.takeIf { endsWord || startsWord }
        }
    },

    /**
     * `->` -> `→`
     */
    SINGLE_RIGHT_ARROW('→', "->"),

    /**
     * `<-` -> `←`
     */
    SINGLE_LEFT_ARROW('←', "<-"),

    /**
     * `=>` -> `⇒`
     */
    DOUBLE_RIGHT_ARROW('⇒', "=>"),

    /**
     * `<==` -> `⇐`
     */
    DOUBLE_LEFT_ARROW('⇐', "<=="),

    /**
     * `>=` -> `≥`
     */
    GREATER_EQUAL('≥', ">="),

    /**
     * `<=` -> `≤`
     */
    LESS_EQUAL('≤', "<="),

    /**
     * `!=` -> `≠`
     */
    NOT_EQUAL('≠', "!="),

    /**
     * `+-` -> `±`
     */
    PLUS_MINUS('±', "+-"),

    /**
     * `'` -> `‘`
     *
     * Must not be preceded by a word and must be followed by a word character.
     */
    TYPOGRAPHIC_LEFT_APOSTROPHE('‘') {
        override fun matches(
            source: CharSequence,
            index: Int,
        ): Int? {
            if (source.getOrNull(index) != '\'') return null
            return 1.takeIf { source.getOrNull(index - 1).isSpaceOrEdge() && source.getOrNull(index + 1).isWordChar() }
        }
    },

    /**
     * `'` -> `’`
     *
     * Must not be preceded by a whitespace.
     */
    TYPOGRAPHIC_RIGHT_APOSTROPHE('’') {
        override fun matches(
            source: CharSequence,
            index: Int,
        ): Int? {
            if (source.getOrNull(index) != '\'') return null
            return 1.takeIf { !source.getOrNull(index - 1).isSpace() }
        }
    },

    /**
     * `"` -> `“`
     *
     * Must not be preceded by a word character and must be followed by a word character.
     */
    TYPOGRAPHIC_LEFT_QUOTATION_MARK('“') {
        override fun matches(
            source: CharSequence,
            index: Int,
        ): Int? {
            if (source.getOrNull(index) != '"') return null
            return 1.takeIf { !source.getOrNull(index - 1).isWordChar() && source.getOrNull(index + 1).isWordChar() }
        }
    },

    /**
     * `"` -> `”`
     *
     * Must not be preceded by a whitespace and not followed by a word character.
     */
    TYPOGRAPHIC_RIGHT_QUOTATION_MARK('”') {
        override fun matches(
            source: CharSequence,
            index: Int,
        ): Int? {
            if (source.getOrNull(index) != '"') return null
            return 1.takeIf { !source.getOrNull(index - 1).isSpace() && !source.getOrNull(index + 1).isWordChar() }
        }
    },
    ;

    /**
     * @param source text to inspect
     * @param index index the sequence must start at
     * @return the amount of characters the sequence spans, or `null` when it does not start at [index]
     */
    open fun matches(
        source: CharSequence,
        index: Int,
    ): Int? = literalAt(source, index, sequence!!, ignoreCase)
}

/**
 * @param source text to inspect
 * @param index index the text must start at
 * @param text literal sequence to look for
 * @param ignoreCase whether the comparison ignores case, as the three parenthesised symbols do
 * @return the length of [text] when it starts at [index], or `null` otherwise
 */
private fun literalAt(
    source: CharSequence,
    index: Int,
    text: String,
    ignoreCase: Boolean = false,
): Int? = text.length.takeIf { source.startsWith(text, index, ignoreCase) }

/**
 * @return whether the character is an ASCII letter, digit or underscore
 */
private fun Char?.isWordChar(): Boolean = this != null && CharCategories.isAsciiWordChar(this)

/**
 * @return whether the character is ASCII whitespace
 */
private fun Char?.isSpace(): Boolean = this != null && CharCategories.isAsciiWhitespace(this)

/**
 * @return whether the character is ASCII whitespace or outside the source, the edges counting as whitespace
 */
private fun Char?.isSpaceOrEdge(): Boolean = this == null || CharCategories.isAsciiWhitespace(this)
