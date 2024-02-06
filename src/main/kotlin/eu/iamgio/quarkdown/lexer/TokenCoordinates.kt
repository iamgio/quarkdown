package eu.iamgio.quarkdown.lexer

/**
 * The position of a token within the source code.
 * @param line line index, starting from 0
 * @param column character index within [line], starting from 0
 */
data class TokenCoordinates(
    val line: Int,
    val column: Int,
)
