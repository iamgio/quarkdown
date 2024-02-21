package eu.iamgio.quarkdown.lexer

import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.common.BlockTokenVisitor
import eu.iamgio.quarkdown.parser.BlockTokenParser

/**
 * A wrapper of a [TokenData] that may be parsed in order to extract information.
 * A token can be parsed into a [eu.iamgio.quarkdown.ast.Node].
 * @param data the wrapped token
 */
sealed class Token(val data: TokenData) {
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

/**
 * Parses a list of tokens into a list of AST [Node]s.
 * @param parser parser to delegate the parsing process to
 */
fun Iterable<Token>.parseAll(parser: BlockTokenParser): List<Node> = this.map { it.parse(parser) }
