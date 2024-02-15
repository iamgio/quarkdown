package eu.iamgio.quarkdown.lexer

/**
 * A parsed token.
 */
interface Token

/**
 * A token that may contain nested tokens.
 */
interface NestableTokenType : Token {
    val children: List<Token>
}
