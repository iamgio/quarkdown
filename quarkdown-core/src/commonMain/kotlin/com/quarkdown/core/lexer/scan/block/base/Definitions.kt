package com.quarkdown.core.lexer.scan.block.base

import com.quarkdown.core.lexer.scan.LABEL_BEGIN
import com.quarkdown.core.lexer.scan.LABEL_END
import com.quarkdown.core.lexer.scan.Scanner
import com.quarkdown.core.lexer.scan.holdsBlankLine
import com.quarkdown.core.lexer.scan.takeEscape

/**
 * Consumes a definition's label: a non-empty run where a backslash escapes the next character and an
 * unescaped bracket is forbidden, closed by a right bracket. The label may span lines, but not a blank one,
 * since a definition ends where a paragraph does.
 * @return the raw label, the closing bracket consumed, or `null` when the brackets close nothing usable
 */
internal fun Scanner.takeDefinitionLabel(): String? =
    attempt {
        val mark = mark()
        repeatWhile { takeEscape() || takeIf { it != LABEL_BEGIN && it != LABEL_END } }
        val label = sliceFrom(mark)
        if (!take(LABEL_END) || label.isBlank() || label.holdsBlankLine()) return@attempt null
        label
    }

/**
 * Consumes a definition's separator: whitespace, then at most one line break followed by whitespace.
 * @return how many characters were consumed, which the optional title rule uses to require a separator
 */
internal fun Scanner.takeSpacesAndOneBreak(): Int {
    val mark = mark()
    takeSpacesAndTabs()
    if (take('\n')) takeSpacesAndTabs()
    return index - mark
}
