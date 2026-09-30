package com.quarkdown.core.lexer.scan.block.quarkdown

import com.quarkdown.core.lexer.scan.block.BlockRecognizer
import com.quarkdown.core.lexer.scan.block.base.CommentRecognizer
import com.quarkdown.core.lexer.scan.block.base.FencedCodeRecognizer
import com.quarkdown.core.lexer.scan.block.base.ThematicBreakRecognizer

/**
 * Inserts the Quarkdown flavor's block recognizers into the base list: the function call right after the
 * comment, the math blocks right after the fenced code, and the page break right after the thematic break.
 * @return the extended list, highest priority first
 */
fun List<BlockRecognizer>.withQuarkdownBlockExtensions(): List<BlockRecognizer> =
    flatMap { recognizer ->
        when (recognizer) {
            CommentRecognizer -> listOf(recognizer, FunctionCallRecognizer)
            FencedCodeRecognizer -> listOf(recognizer, MultilineMathRecognizer, OnelineMathRecognizer)
            ThematicBreakRecognizer -> listOf(recognizer, PageBreakRecognizer)
            else -> listOf(recognizer)
        }
    }
