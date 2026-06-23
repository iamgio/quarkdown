package com.quarkdown.stdlib.internal

import com.quarkdown.core.ast.InlineContent
import com.quarkdown.core.ast.Node
import com.quarkdown.core.ast.base.TextNode
import com.quarkdown.core.ast.base.inline.Text
import com.quarkdown.core.ast.rewriter.withChildren

/**
 * Recursively walks this node and returns the equivalent inline content where every substring matching [regex]
 * found within plain [Text] leaves is replaced by the output of [replacement].
 *
 * Non-text children are returned unchanged. [TextNode]s are descended into, preserving their structure
 * around the transformed inner content.
 *
 * @param regex pattern to match in plain text leaves
 * @param replacement maps each matched substring to the inline content that should replace it
 * @return inline content with matches replaced
 */
internal fun Node.replaceMatches(
    regex: Regex,
    replacement: (String) -> InlineContent,
): InlineContent =
    when (this) {
        is Text -> this.text.replaceMatches(regex, replacement)
        is TextNode -> listOf(this.withChildren(this.text.flatMap { it.replaceMatches(regex, replacement) }))
        else -> listOf(this)
    }

/**
 * Splits this string around matches of [regex] and produces inline content where each match is replaced
 * by [replacement] and the surrounding segments are wrapped in plain [Text] nodes.
 *
 * Returns a singleton list containing the original string as a [Text] node if no matches are found.
 */
private fun String.replaceMatches(
    regex: Regex,
    replacement: (String) -> InlineContent,
): InlineContent =
    buildList {
        var cursor = 0
        for (match in regex.findAll(this@replaceMatches)) {
            if (match.range.first > cursor) {
                add(Text(substring(cursor, match.range.first)))
            }
            addAll(replacement(match.value))
            cursor = match.range.last + 1
        }
        if (cursor < length) {
            add(Text(substring(cursor)))
        }
    }
