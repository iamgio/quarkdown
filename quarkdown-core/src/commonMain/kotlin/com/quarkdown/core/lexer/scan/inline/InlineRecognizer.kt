package com.quarkdown.core.lexer.scan.inline

import com.quarkdown.core.lexer.Token
import com.quarkdown.core.lexer.TokenData

/**
 * What an [InlineRecognizer] produced at a position.
 * @param piece the produced piece
 * @param end index the scan resumes at, which is just past the piece for every recognizer except the
 *            function call ones, whose token is zero-width and whose end comes from their walker
 */
class InlineMatch(
    val piece: InlinePiece,
    val end: Int,
) {
    /**
     * @param token the produced token
     * @param end index just past the match
     */
    constructor(token: Token, end: Int) : this(InlinePiece.Finished(token), end)

    /**
     * The produced token, for the recognizers that produce one rather than a delimiter run.
     */
    val token: Token get() = (piece as InlinePiece.Finished).token
}

/**
 * A strategy that attempts to produce an inline piece at a position.
 *
 * Recognizers receive the padded source and an index and never mutate anything, so a failed attempt costs
 * nothing and cannot leave the scan in a wrong place. They are tried in priority order at every index, and
 * the scan moves to the next index when none matches, so the leftmost position wins and, within a position,
 * the highest priority recognizer does.
 */
fun interface InlineRecognizer {
    /**
     * @param source the source being scanned, followed by one padding newline
     * @param index index to attempt the match at
     * @return the match, or `null` when this recognizer does not apply there
     */
    fun recognize(
        source: CharSequence,
        index: Int,
    ): InlineMatch?
}

/**
 * Builds a match whose token covers a source span.
 * @param source the source being scanned
 * @param start index of the match's first character
 * @param end index just past the match's last character
 * @param wrap constructor of the token
 * @return the resulting match
 */
fun inlineMatch(
    source: CharSequence,
    start: Int,
    end: Int,
    wrap: (TokenData) -> Token,
): InlineMatch = InlineMatch(wrap(TokenData(source.substring(start, end), start until end)), end)
