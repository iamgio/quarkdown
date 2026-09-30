package com.quarkdown.core.lexer.scan.inline

import com.quarkdown.core.lexer.Token

/**
 * One element of the inline scan's first pass. Emphasis pairing needs the whole run before it can decide
 * what nests inside what, so the first pass stops at these three shapes and the second pass resolves them.
 */
sealed interface InlinePiece {
    /**
     * A token a recognizer already produced.
     * @param token the produced token
     */
    class Finished(
        val token: Token,
    ) : InlinePiece

    /**
     * A span no recognizer claimed, which becomes plain text unless it ends up inside an emphasis pair.
     * @param start index of the span's first character
     * @param end index just past the span's last character
     */
    class Raw(
        val start: Int,
        val end: Int,
    ) : InlinePiece

    /**
     * An unresolved delimiter run.
     * @param run the classified run
     */
    class Delimiter(
        val run: DelimiterRun,
    ) : InlinePiece
}
