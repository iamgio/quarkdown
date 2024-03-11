package eu.iamgio.quarkdown.lexer

import eu.iamgio.quarkdown.parser.visitor.TokenVisitor

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
    abstract fun <T> accept(visitor: TokenVisitor<T>): T
}

/**
 * Accepts a list of tokens to a shared visitor.
 * @param visitor the visitor to visit for each token.
 * @return the list of results from each visit
 */
fun <T> Iterable<Token>.acceptAll(visitor: TokenVisitor<T>): List<T> = this.map { it.accept(visitor) }
