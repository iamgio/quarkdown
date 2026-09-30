package com.quarkdown.core.lexer.scan.inline

import com.quarkdown.core.lexer.Lexer
import com.quarkdown.core.lexer.Token
import com.quarkdown.core.lexer.TokenData
import com.quarkdown.core.util.normalizeLineSeparators

/**
 * A [Lexer] that scans inline-level tokens in two passes.
 *
 * The first pass walks the source character by character, trying each recognizer in order at every index and
 * turning the spans nothing claimed into [InlinePiece.Raw]. The second pass resolves the delimiter runs the
 * first pass left behind into nested emphasis tokens and flattens everything else into plain text.
 *
 * As in [com.quarkdown.core.lexer.scan.block.BlockScanLexer], the scanned text is the source followed by one
 * newline, so a construct that needs a line terminator is still recognized on the last line; the loop stops
 * on the unpadded length.
 *
 * @param source the content to be tokenized
 * @param recognizers the recognizers to try, in descending order of priority
 * @param fill constructor of the token that covers unclaimed spans, or `null` to leave gaps
 */
class InlineScanLexer(
    source: CharSequence,
    private val recognizers: List<InlineRecognizer>,
    private val fill: ((TokenData) -> Token)?,
) : Lexer {
    override val source: CharSequence = source.normalizeLineSeparators()

    private val padded: String = "${this.source}\n"

    override fun tokenize(): Sequence<Token> = EmphasisResolver(padded, fill).resolve(scanPieces()).asSequence()

    private fun scanPieces(): List<InlinePiece> =
        buildList {
            var index = 0
            var rawStart = 0
            while (index < source.length) {
                var probe = index
                var match: InlineMatch? = null
                while (probe < padded.length) {
                    match = recognizers.firstNotNullOfOrNull { it.recognize(padded, probe) }
                    if (match != null) break
                    probe++
                }
                val found = match ?: break
                if (probe > rawStart) add(InlinePiece.Raw(rawStart, probe))
                add(found.piece)
                if (found.end <= index) break
                index = found.end
                rawStart = found.end
            }
            if (rawStart < source.length) add(InlinePiece.Raw(rawStart, source.length))
        }
}
