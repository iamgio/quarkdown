package eu.iamgio.quarkdown.parser

import eu.iamgio.quarkdown.lexer.Token

/**
 *
 */
interface TokenParser<T> {
    fun parse(raw: Token): T
}
