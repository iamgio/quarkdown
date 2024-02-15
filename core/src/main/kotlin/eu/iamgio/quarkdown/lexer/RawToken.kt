package eu.iamgio.quarkdown.lexer

/**
 * A single, usually small, component of the source code that stores a chunk of information.
 * For instance, the Markdown code `Hello _Quarkdown_` contains the tokens `Hello `, `_`, `Quarkdown`, `_`.
 * Raw tokens can be parsed into processed tokens.
 * @param text the substring extracted from the source code, also known as _lexeme_.
 * @param position location of the token within the source code
 */
data class RawToken(
    val text: String,
    val position: IntRange,
)
