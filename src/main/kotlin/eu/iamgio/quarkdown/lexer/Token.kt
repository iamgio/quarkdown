package eu.iamgio.quarkdown.lexer

/**
 * A single, usually small, component of the source code that stores a chunk of information.
 * For instance, the Markdown code `Hello _Quarkdown_` contains the tokens `Hello `, `_`, `Quarkdown`, `_`.
 * @param text the substring extracted from the source code, also known as _lexeme_.
 * @param literal the literal value stored in this token, such as a string or number.
 * @param coordinates location of the token within the source code
 */
data class Token(
    val text: String,
    val literal: Any,
    val coordinates: TokenCoordinates,
)
