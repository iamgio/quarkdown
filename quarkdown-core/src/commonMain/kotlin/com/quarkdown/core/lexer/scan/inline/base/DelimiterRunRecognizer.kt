package com.quarkdown.core.lexer.scan.inline.base

import com.quarkdown.core.lexer.scan.inline.DelimiterRuns
import com.quarkdown.core.lexer.scan.inline.InlineMatch
import com.quarkdown.core.lexer.scan.inline.InlinePiece
import com.quarkdown.core.lexer.scan.inline.InlineRecognizer

private const val DELIMITER_CHARS = "*_~"

/**
 * Recognizes a delimiter run of asterisks, underscores or tildes.
 * It claims the whole run without deciding anything: pairing needs the rest of the line, so it happens in
 * the inline scanner's second pass, and a run that pairs with nothing ends up as plain text.
 *
 * Every run is claimed whole, including one that can neither open nor close, so the scan never resumes inside
 * a run and no shorter suffix of one is ever classified on its own. A delimiter right after an escape, as in
 * `foo *\**`, is a genuine new run and is classified as one.
 */
object DelimiterRunRecognizer : InlineRecognizer {
    override fun recognize(
        source: CharSequence,
        index: Int,
    ): InlineMatch? {
        val char = source.getOrNull(index) ?: return null
        if (char !in DELIMITER_CHARS) return null
        val run = DelimiterRuns.classify(source, index, char) ?: return null
        return InlineMatch(InlinePiece.Delimiter(run), index + run.originalLength)
    }
}
