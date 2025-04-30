package com.quarkdown.core.lexer

/**
 * The position of a token within the source code.
 * @param line line index, starting from 0
 * @param column character index within [line], starting from 0
 */
data class TokenCoordinates(
    val line: Int,
    val column: Int,
)

/**
 * Converts a range of indexes within a string to its `(x, y)` coordinates.
 * @param source source to extract coordinates from
 * @return `(x, y)` coordinates
 */
fun IntRange.toCoordinates(source: CharSequence): TokenCoordinates {
    TODO()
}
