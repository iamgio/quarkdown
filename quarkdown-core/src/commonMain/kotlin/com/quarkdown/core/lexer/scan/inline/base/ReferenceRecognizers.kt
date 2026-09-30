package com.quarkdown.core.lexer.scan.inline.base

import com.quarkdown.core.lexer.scan.DEFINITION_MARKER
import com.quarkdown.core.lexer.scan.FOOTNOTE_MARKER
import com.quarkdown.core.lexer.scan.LABEL_BEGIN
import com.quarkdown.core.lexer.scan.LABEL_END
import com.quarkdown.core.lexer.scan.Scanner
import com.quarkdown.core.lexer.scan.inline.InlineMatch
import com.quarkdown.core.lexer.scan.inline.InlineRecognizer
import com.quarkdown.core.lexer.scan.inline.inlineMatch
import com.quarkdown.core.lexer.scan.inline.takeBacktickSpan
import com.quarkdown.core.lexer.scan.inline.takeLabel
import com.quarkdown.core.lexer.scan.takeEscape
import com.quarkdown.core.lexer.tokens.ReferenceFootnoteToken
import com.quarkdown.core.lexer.tokens.ReferenceLinkToken

/**
 * A reference in a second bracket pair.
 * @param value the raw reference, or `null` when the brackets were empty, blank or absent
 */
private class ReferenceTail(
    val value: String?,
)

/**
 * Recognizes a link whose destination lives in a link definition.
 * The second bracket pair and the reference inside it are both optional, so the tail always parses and the
 * label therefore ends at its first unescaped closing bracket.
 */
object ReferenceLinkRecognizer : InlineRecognizer {
    override fun recognize(
        source: CharSequence,
        index: Int,
    ): InlineMatch? {
        if (source.getOrNull(index) != LABEL_BEGIN) return null
        val scanner = Scanner(source, index = index)
        val (label, tail) = scanner.takeLabel { takeReferenceTail() } ?: return null
        return inlineMatch(source, index, scanner.index) { ReferenceLinkToken(it, label = label, reference = tail.value) }
    }
}

/**
 * Recognizes a footnote reference, optionally carrying its whole definition.
 */
object ReferenceFootnoteRecognizer : InlineRecognizer {
    override fun recognize(
        source: CharSequence,
        index: Int,
    ): InlineMatch? {
        if (source.getOrNull(index) != LABEL_BEGIN) return null
        val scanner = Scanner(source, index = index)
        if (!scanner.take(LABEL_BEGIN) || !scanner.take(FOOTNOTE_MARKER)) return null
        val (label, definition) = scanner.takeFootnoteBody()
        if (!scanner.take(LABEL_END)) return null
        return inlineMatch(source, index, scanner.index) { ReferenceFootnoteToken(it, label = label, definition = definition) }
    }
}

/**
 * Consumes the reference link's optional second bracket pair, which always parses: an absent, empty or blank
 * pair simply yields no reference. The `step` is what makes `[]` consume its two characters while a malformed
 * `[x` consumes none.
 * @return the tail
 */
private fun Scanner.takeReferenceTail(): ReferenceTail {
    var reference: String? = null
    step {
        if (!take(LABEL_BEGIN)) return@step false
        if (take(LABEL_END)) return@step true
        val mark = mark()
        repeatWhile { takeEscape() || takeIf { it != LABEL_BEGIN && it != LABEL_END } }
        val candidate = sliceFrom(mark)
        if (!take(LABEL_END) || candidate.isBlank()) return@step false
        reference = candidate
        true
    }
    return ReferenceTail(reference)
}

/**
 * Consumes the footnote's label and, when one follows, its all-in-one definition. A colon that introduces no
 * usable definition belongs to the label: `[^a:]` is a footnote labelled `a:`, not a malformed one.
 * @return the raw label and definition
 */
private fun Scanner.takeFootnoteBody(): Pair<String, String?> {
    val mark = mark()
    while (true) {
        repeatWhile {
            takeEscape() || takeBacktickSpan() || takeIf { it != LABEL_BEGIN && it != LABEL_END && it != DEFINITION_MARKER }
        }
        val labelEnd = mark()
        val definition = takeFootnoteDefinition()
        if (definition != null) return source.substring(mark, labelEnd) to definition
        if (!take(DEFINITION_MARKER)) return source.substring(mark, labelEnd) to null
    }
}

/**
 * Consumes the footnote's optional definition, introduced by a colon.
 * @return the raw definition, or `null` when there is none
 */
private fun Scanner.takeFootnoteDefinition(): String? =
    attempt {
        if (!take(DEFINITION_MARKER)) return@attempt null
        takeSpacesAndTabs()
        val mark = mark()
        repeatWhile { takeEscape() || takeBacktickSpan() || takeIf { it != LABEL_BEGIN && it != LABEL_END } }
        sliceFrom(mark).takeIf { it.isNotEmpty() }
    }
