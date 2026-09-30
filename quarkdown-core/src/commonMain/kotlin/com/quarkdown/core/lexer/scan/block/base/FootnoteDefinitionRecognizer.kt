package com.quarkdown.core.lexer.scan.block.base

import com.quarkdown.core.lexer.scan.DEFINITION_MARKER
import com.quarkdown.core.lexer.scan.FOOTNOTE_MARKER
import com.quarkdown.core.lexer.scan.LABEL_BEGIN
import com.quarkdown.core.lexer.scan.LineCursor
import com.quarkdown.core.lexer.scan.MAX_BLOCK_INDENT
import com.quarkdown.core.lexer.scan.Scanner
import com.quarkdown.core.lexer.scan.block.BlockMatch
import com.quarkdown.core.lexer.scan.block.BlockRecognizer
import com.quarkdown.core.lexer.scan.block.Interruption
import com.quarkdown.core.lexer.scan.block.extendWhile
import com.quarkdown.core.lexer.scan.block.match
import com.quarkdown.core.lexer.tokens.FootnoteDefinitionToken

/**
 * Recognizes a footnote definition.
 * Its text follows the paragraph continuation rule, so the definition ends where a paragraph does.
 * @param interruption what ends the definition, supplied by the flavor
 */
class FootnoteDefinitionRecognizer(
    private val interruption: Interruption,
) : BlockRecognizer {
    override fun open(cursor: LineCursor): BlockMatch? {
        val line = cursor.current
        val isFootnote =
            line.indent <= MAX_BLOCK_INDENT &&
                line.charAt(line.contentStart) == LABEL_BEGIN &&
                line.charAt(line.contentStart + 1) == FOOTNOTE_MARKER
        if (!isFootnote) return null
        val scanner = Scanner(line.sourceOf(), index = line.contentStart + 2)
        val label = scanner.takeDefinitionLabel() ?: return null
        if (!scanner.take(DEFINITION_MARKER)) return null
        scanner.takeWhile { it == ' ' }
        val extent = cursor.extendWhile { !it.current.isEmpty && !interruption.interrupts(it) }
        val content = line.sourceSlice(minOf(scanner.index, extent.endIndex), extent.endIndex)
        return cursor.match(extent) { FootnoteDefinitionToken(it, label, content) }
    }
}
