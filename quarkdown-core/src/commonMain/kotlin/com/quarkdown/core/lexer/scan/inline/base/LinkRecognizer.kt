package com.quarkdown.core.lexer.scan.inline.base

import com.quarkdown.core.lexer.scan.LABEL_BEGIN
import com.quarkdown.core.lexer.scan.Scanner
import com.quarkdown.core.lexer.scan.inline.InlineMatch
import com.quarkdown.core.lexer.scan.inline.InlineRecognizer
import com.quarkdown.core.lexer.scan.inline.inlineMatch
import com.quarkdown.core.lexer.scan.inline.takeLabel
import com.quarkdown.core.lexer.scan.inline.takeLinkTarget
import com.quarkdown.core.lexer.tokens.LinkToken

/**
 * Recognizes a link with its label in brackets and its destination in parentheses.
 */
object LinkRecognizer : InlineRecognizer {
    override fun recognize(
        source: CharSequence,
        index: Int,
    ): InlineMatch? {
        if (source.getOrNull(index) != LABEL_BEGIN) return null
        val scanner = Scanner(source, index = index)
        val (label, target) = scanner.takeLabel { takeLinkTarget() } ?: return null
        return inlineMatch(source, index, scanner.index) { LinkToken(it, label = label, url = target.url, title = target.title) }
    }
}
