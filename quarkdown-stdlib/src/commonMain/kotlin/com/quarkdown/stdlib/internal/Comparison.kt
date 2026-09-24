package com.quarkdown.stdlib.internal

import com.quarkdown.core.ast.InlineMarkdownContent
import com.quarkdown.core.ast.MarkdownContent
import com.quarkdown.core.util.node.toPlainText

/**
 * Lets Markdown content be compared to its plain-text form
 *
 * Returns `null` for values that have no plain-text representation, so callers can fall back
 * to equality on the underlying value.
 *
 * @return the plain-text projection of [value] if one is defined, `null` otherwise.
 */
internal fun asComparablePlainText(value: Any?): String? =
    when (value) {
        is String -> value
        is Number -> value.toString()
        is InlineMarkdownContent -> value.children.toPlainText()
        is MarkdownContent -> value.children.toPlainText()
        else -> null
    }
