package eu.iamgio.quarkdown.lexer

/**
 * Data of a single, usually small, substring of the source code that stores a chunk of information.
 * For instance, the Markdown code `Hello _Quarkdown_` contains the tokens `Hello `, `_`, `Quarkdown`, `_`.
 * @param text the substring extracted from the source code, also known as _lexeme_.
 * @param position location of the token within the source code
 * @see Token
 */
open class TokenData(
    val text: String,
    val position: IntRange,
)
