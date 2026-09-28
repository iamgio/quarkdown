package com.quarkdown.core.lexer.scan.block.base

import com.quarkdown.core.lexer.scan.DEFINITION_MARKER
import com.quarkdown.core.lexer.scan.DESTINATION_BEGIN
import com.quarkdown.core.lexer.scan.LABEL_BEGIN
import com.quarkdown.core.lexer.scan.LineCursor
import com.quarkdown.core.lexer.scan.MAX_BLOCK_INDENT
import com.quarkdown.core.lexer.scan.Scanner
import com.quarkdown.core.lexer.scan.block.BlockEnd
import com.quarkdown.core.lexer.scan.block.BlockMatch
import com.quarkdown.core.lexer.scan.block.BlockRecognizer
import com.quarkdown.core.lexer.scan.block.extent
import com.quarkdown.core.lexer.scan.block.match
import com.quarkdown.core.lexer.scan.takeAngleDestination
import com.quarkdown.core.lexer.scan.takeDelimitedTitle
import com.quarkdown.core.lexer.scan.takePlainDestination
import com.quarkdown.core.lexer.tokens.LinkDefinitionToken

/**
 * Recognizes a referenceable link definition.
 * The URL may start on the line after the colon and the title on the line after the URL, so a definition
 * spans up to three lines. The block absorbs the empty lines that follow it.
 *
 * A footnote label parses as an ordinary label here: the footnote definition recognizer comes first in every
 * lexer, so it claims `[^label]:` before this one is tried, and `[^]:`, whose footnote label is empty, is
 * left to this recognizer as a link definition labelled `^`.
 */
object LinkDefinitionRecognizer : BlockRecognizer {
    override fun open(cursor: LineCursor): BlockMatch? {
        val line = cursor.current
        if (line.indent > MAX_BLOCK_INDENT || line.charAt(line.contentStart) != LABEL_BEGIN) return null
        val scanner = Scanner(line.sourceOf(), index = line.contentStart)
        if (!scanner.take(LABEL_BEGIN)) return null
        val label = scanner.takeDefinitionLabel() ?: return null
        if (!scanner.take(DEFINITION_MARKER)) return null
        scanner.takeSpacesAndOneBreak()
        val url = scanner.takeDefinitionUrl() ?: return null
        // A title only counts when the definition ends with it; otherwise the definition ends at the URL,
        // and anything past either of them makes the whole line a paragraph.
        val title = scanner.attempt { takeDefinitionTitle()?.takeIf { endsDefinitionLine() } }
        if (title == null && !scanner.endsDefinitionLine()) return null
        scanner.takeSpacesAndTabs()
        val lineCount = cursor.lineCountThrough(scanner.index) ?: return null
        val extent = cursor.extent(lineCount, BlockEnd.AFTER_EMPTY_LINES)
        return cursor.match(extent) { LinkDefinitionToken(it, label, url, title) }
    }
}

/**
 * A definition may be followed by whitespace only, since it takes its whole last line.
 * @return whether the rest of the scanner's line holds nothing but spaces and tabs
 */
private fun Scanner.endsDefinitionLine(): Boolean =
    lookahead {
        takeSpacesAndTabs()
        isAtEnd || peek() == '\n'
    }

/**
 * A URL is either enclosed in angle brackets, where it may hold spaces, or a bare destination, which reads
 * exactly as an inline link's does: up to the next whitespace, with balanced parentheses. A `<` that never
 * closes on the line, or an unbalanced parenthesis, makes the definition fail.
 * @return the raw URL, angle brackets included when it had them, or `null` when there is none
 */
private fun Scanner.takeDefinitionUrl(): String? =
    when (peek()) {
        null -> null
        DESTINATION_BEGIN -> takeAngleDestination(allowEmpty = true)
        else -> takePlainDestination().takeIf { it.isNotEmpty() }
    }

/**
 * A title needs at least one space, tab or line break before it.
 * @return the raw title including its delimiters, or `null` when there is none
 */
private fun Scanner.takeDefinitionTitle(): String? =
    attempt {
        if (takeSpacesAndOneBreak() == 0) return@attempt null
        takeDelimitedTitle()
    }
