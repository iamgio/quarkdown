package com.quarkdown.core.lexer.scan.block

import com.quarkdown.core.lexer.Lexer
import com.quarkdown.core.lexer.Token
import com.quarkdown.core.lexer.scan.Line
import com.quarkdown.core.lexer.scan.LineCursor
import com.quarkdown.core.lexer.scan.Lines
import com.quarkdown.core.util.normalizeLineSeparators

/**
 * A [Lexer] that scans block-level tokens line by line, trying each recognizer in order at every line start.
 *
 * The scanned text is the source followed by one newline, and scanning stops once a block has ended at or
 * past the source's own length. That trailing newline is what lets a construct whose terminator expects one
 * still close at the end of input, and it is why a source that already ends in a newline yields one final
 * token for the empty line after it.
 *
 * @param source the content to be tokenized
 * @param recognizers the recognizers to try, in descending order of priority
 */
class BlockScanLexer(
    source: CharSequence,
    private val recognizers: List<BlockRecognizer>,
) : Lexer {
    override val source: CharSequence = source.normalizeLineSeparators()

    private val padded: CharSequence = "${this.source}\n"

    private val paddedLines: List<Line> = Lines.of(padded)

    private val midLineRecognizers: List<BlockRecognizer> = recognizers.filterNot { it.isLineAnchored }

    override fun tokenize(): Sequence<Token> =
        sequence {
            var position = 0
            var lineIndex = 0
            while (position < source.length && lineIndex < paddedLines.size) {
                val resumesMidLine = position > paddedLines[lineIndex].start
                var probe = lineIndex
                var match: BlockMatch? = null
                while (probe < paddedLines.size) {
                    val midLine = resumesMidLine && probe == lineIndex
                    val lines = if (midLine) resumedLines(lineIndex, position) else paddedLines
                    val candidates = if (midLine) midLineRecognizers else recognizers
                    val cursor = LineCursor(lines, probe)
                    match = candidates.firstNotNullOfOrNull { it.open(cursor) }
                    if (match != null) break
                    probe++
                }
                val found = match ?: break
                yield(found.token)
                if (found.scanEnd <= position) break
                position = found.scanEnd
                val lastLineIndex = probe + found.lineCount - 1
                lineIndex = if (position < paddedLines[lastLineIndex].end) lastLineIndex else lastLineIndex + 1
            }
        }

    /**
     * @param lineIndex index of the line the scan resumes inside
     * @param position index the scan resumes at, past that line's start
     * @return the lines with that one narrowed to its remainder, so the recognizers see the rest of the line
     *         as if it were a line of its own
     */
    private fun resumedLines(
        lineIndex: Int,
        position: Int,
    ): List<Line> {
        val line = paddedLines[lineIndex]
        return ResumedLines(paddedLines, lineIndex, Line(padded, position, line.end, line.nextStart))
    }
}

/**
 * A view of [base] that exposes [replacement] at [at], so resuming inside a line costs no copy of the line
 * list.
 * @param base the whole line list
 * @param at index of the overridden line
 * @param replacement line to expose at [at]
 */
private class ResumedLines(
    private val base: List<Line>,
    private val at: Int,
    private val replacement: Line,
) : AbstractList<Line>() {
    override val size: Int get() = base.size

    override fun get(index: Int): Line = if (index == at) replacement else base[index]
}
