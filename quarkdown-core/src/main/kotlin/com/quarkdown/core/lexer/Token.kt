package com.quarkdown.core.lexer

import com.quarkdown.core.visitor.token.TokenVisitor

/**
 * A wrapper of a [TokenData] that may be parsed in order to extract information.
 * A token can be parsed into a [com.quarkdown.core.ast.Node].
 * @param data the wrapped token
 */
abstract class Token(
    val data: TokenData,
) {
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
