package com.quarkdown.core.lexer.scan

private val TITLE_DELIMITERS = mapOf('"' to '"', '\'' to '\'', '(' to ')')

private val TITLE_OPENINGS = TITLE_DELIMITERS.entries.associate { (opening, closing) -> closing to opening }

/**
 * @param closing character a title might end with
 * @return the delimiter that opens a title [closing] closes, or `null` when it closes none
 */
internal fun titleOpeningFor(closing: Char): Char? = TITLE_OPENINGS[closing]

/**
 * Consumes a title enclosed in double quotes, single quotes or parentheses. A backslash escapes the next
 * character, and the closing delimiter may not appear unescaped inside. The title may span lines, but not a
 * blank one, since the construct it belongs to ends where a paragraph does.
 * @return the title including its delimiters, or `null` when none starts here
 */
fun Scanner.takeDelimitedTitle(): String? =
    attempt {
        val mark = mark()
        val opening = peek() ?: return@attempt null
        val closing = TITLE_DELIMITERS[opening] ?: return@attempt null
        take(opening)
        repeatWhile { takeEscape() || takeIf { it != closing } }
        if (!take(closing)) return@attempt null
        sliceFrom(mark).takeUnless { it.holdsBlankLine() }
    }
