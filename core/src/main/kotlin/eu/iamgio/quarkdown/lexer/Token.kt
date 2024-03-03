package eu.iamgio.quarkdown.lexer

import eu.iamgio.quarkdown.parser.visitor.BlockTokenVisitor

/**
 * A wrapper of a [TokenData] that may be parsed in order to extract information.
 * A token can be parsed into a [eu.iamgio.quarkdown.ast.Node].
 * @param data the wrapped token
 */
sealed class Token(val data: TokenData) {
    /**
     * Accepts a visitor.
     * @param T output type of the visitor
     * @return output of the visit
     */
    abstract fun <T> accept(visitor: BlockTokenVisitor<T>): T // TODO change to general TokenVisitor
}

/**
 * Accepts a list of tokens to a shared visitor.
 * @param visitor the visitor to visit for each token.
 * @return the list of results from each visit
 */
fun <T> Iterable<Token>.acceptAll(visitor: BlockTokenVisitor<T>): List<T> = this.map { it.accept(visitor) }
