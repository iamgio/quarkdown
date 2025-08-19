package com.quarkdown.lsp.util

import com.quarkdown.core.util.substringWithinBounds
import org.eclipse.lsp4j.Position

/**
 * @param text the full text to search within
 * @param position the position (line and character) up to which to slice
 * @param delimiter the delimiter to search for in the line
 * @param includeDelimiter whether to include the delimiter in the result
 * @return the substring after the last delimiter before the position,
 * or `null` if not found, or empty if the delimiter is at the end
 */
fun sliceFromDelimiterToPosition(
    text: String,
    position: Position,
    delimiter: String,
    includeDelimiter: Boolean = false,
): String? {
    // Current line until position.
    val linePrefix =
        text
            .lines()
            .getOrNull(position.line)
            ?.takeIf { position.character >= 0 }
            ?.substringWithinBounds(0, position.character)
            ?: return null

    // Extracts the text between the last occurrence of the delimiter and the position.
    val delimiterIndex = linePrefix.lastIndexOf(delimiter).takeIf { it >= 0 } ?: return null
    val delta = if (includeDelimiter) 0 else delimiter.length
    val sliced =
        linePrefix
            .takeIf { it.length > delimiterIndex + delta }
            ?.substring(delimiterIndex + delta)
            ?: ""

    return sliced
}
