package com.quarkdown.core.lexer.scan.inline

import com.quarkdown.core.lexer.scan.BACKTICK
import com.quarkdown.core.lexer.scan.ESCAPE
import com.quarkdown.core.lexer.scan.LABEL_BEGIN
import com.quarkdown.core.lexer.scan.LABEL_END
import com.quarkdown.core.lexer.scan.Scanner
import com.quarkdown.core.lexer.scan.takeEscape

/**
 * Consumes the bracketed label shared by links, reference links, images and reference images.
 *
 * The label is lazy: at every candidate closing bracket it asks [closes] whether the rest of the construct
 * parses, and stops at the first bracket where it does. [closes] runs on this same scanner, positioned just
 * past that bracket, and returning `null` from it rewinds and keeps scanning.
 *
 * @param closes parses the rest of the construct, returning `null` to reject this closing bracket
 * @return the raw label and what [closes] produced, or `null` when no label closes here
 */
fun <T : Any> Scanner.takeLabel(closes: Scanner.() -> T?): Pair<String, T>? =
    attempt {
        if (!take(LABEL_BEGIN)) return@attempt null
        val mark = mark()
        var closed: Pair<String, T>? = null
        while (closed == null && !isAtEnd) {
            if (peek() == LABEL_END) {
                val label = sliceFrom(mark)
                closed =
                    attempt {
                        take(LABEL_END)
                        closes()
                    }?.let { label to it }
            }
            if (closed == null && !takeLabelElement()) return@attempt null
        }
        closed
    }

/**
 * @return whether one label element was consumed: a one-level bracketed group, an escape, a backtick span, or
 *         any character that is none of the brackets, the backslash or the backtick
 */
private fun Scanner.takeLabelElement(): Boolean =
    takeEscape() ||
        takeBacktickSpan() ||
        takeNestedBrackets() ||
        takeIf { it != LABEL_BEGIN && it != LABEL_END && it != ESCAPE && it != BACKTICK }

/**
 * Consumes a backtick-delimited span, which a label treats as opaque.
 * @return whether one was consumed
 */
fun Scanner.takeBacktickSpan(): Boolean =
    step {
        take(BACKTICK) &&
            run {
                takeWhile { it != BACKTICK }
                take(BACKTICK)
            }
    }

/**
 * Consumes one level of bracketed content inside a label, which admits no further nesting.
 * @return whether a nested group was consumed
 */
private fun Scanner.takeNestedBrackets(): Boolean =
    step {
        take(LABEL_BEGIN) &&
            run {
                repeatWhile { takeEscape() || takeIf { it != LABEL_BEGIN && it != LABEL_END } }
                take(LABEL_END)
            }
    }
