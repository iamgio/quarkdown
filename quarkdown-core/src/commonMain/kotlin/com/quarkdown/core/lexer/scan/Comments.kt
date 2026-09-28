package com.quarkdown.core.lexer.scan

private const val COMMENT_OPEN = "<!--"

private const val COMMENT_CLOSE = "-->"

private const val ABRUPT_CLOSE = ">"

private const val ABRUPT_DASH_CLOSE = "->"

/**
 * A comment closes either abruptly, with `>` or `->` right after the opening, or at the first `-->`.
 * @param source text to inspect
 * @param index index the comment must start at
 * @return index just past the comment's closing delimiter, or `null` when no comment starts at [index]
 */
fun commentEndAt(
    source: CharSequence,
    index: Int,
): Int? {
    if (!source.startsWith(COMMENT_OPEN, index)) return null
    val afterOpen = index + COMMENT_OPEN.length
    if (source.startsWith(ABRUPT_CLOSE, afterOpen)) return afterOpen + ABRUPT_CLOSE.length
    if (source.startsWith(ABRUPT_DASH_CLOSE, afterOpen)) return afterOpen + ABRUPT_DASH_CLOSE.length
    val close = source.indexOf(COMMENT_CLOSE, afterOpen)
    return if (close < 0) null else close + COMMENT_CLOSE.length
}

/**
 * Removes every comment, leaving the rest of the text untouched. A `<!--` that never closes is text, not a
 * comment, so it survives.
 * @return this text without its comments, or this very text when it holds none
 */
fun CharSequence.withoutComments(): String {
    var start = indexOf(COMMENT_OPEN)
    if (start < 0) return toString()
    val stripped = StringBuilder(length)
    var kept = 0
    while (start >= 0) {
        val end = commentEndAt(this, start) ?: break
        stripped.append(this, kept, start)
        kept = end
        start = indexOf(COMMENT_OPEN, kept)
    }
    return stripped.append(this, kept, length).toString()
}
