package eu.iamgio.quarkdown.lexer

import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.common.BlockTokenVisitor
import eu.iamgio.quarkdown.parser.BlockTokenParser

/**
 * A wrapper of a [Token] that may be parsed by a specific parser in order to extract information.
 * @param data the wrapped token
 */
sealed class TokenWrapper(val data: Token) {
    /**
     * Parses this token into an AST [Node].
     * @param parser parser to delegate the parsing process to
     */
    fun parse(parser: BlockTokenParser): Node = this.accept(parser)

    /**
     * Accepts a visitor.
     * @param T output type of the visitor
     * @return output of the visit
     */
    abstract fun <T> accept(visitor: BlockTokenVisitor<T>): T // TODO change to general TokenVisitor
}
