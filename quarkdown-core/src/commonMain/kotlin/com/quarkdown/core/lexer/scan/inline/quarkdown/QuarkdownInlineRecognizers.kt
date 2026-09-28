package com.quarkdown.core.lexer.scan.inline.quarkdown

import com.quarkdown.core.lexer.scan.inline.InlineRecognizer

/**
 * Inserts the Quarkdown flavor's inline recognizers right before the critical content recognizer, which is
 * always last.
 * @return the extended list, highest priority first
 */
fun List<InlineRecognizer>.withQuarkdownInlineExtensions(): List<InlineRecognizer> =
    dropLast(1) + InlineFunctionCallRecognizer + InlineMathRecognizer +
        TextSymbolReplacement.entries.map { it.toRecognizer() } + last()
