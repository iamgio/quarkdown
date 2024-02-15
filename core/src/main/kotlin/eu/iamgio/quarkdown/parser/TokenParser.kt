package eu.iamgio.quarkdown.parser

import eu.iamgio.quarkdown.lexer.RawToken

/**
 *
 */
interface TokenParser<T> {
    fun parse(raw: RawToken): T
}
