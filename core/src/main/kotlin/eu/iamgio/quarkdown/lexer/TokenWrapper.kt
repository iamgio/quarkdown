package eu.iamgio.quarkdown.lexer

import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.parser.BlockTokenParser

/**
 * A wrapper of a [Token] that may be parsed by a specific parser in order to extract information.
 * @param data the wrapped token
 */
sealed class TokenWrapper(val data: Token) {
    abstract fun parse(parser: BlockTokenParser): Node // TODO change to general TokenParser
}