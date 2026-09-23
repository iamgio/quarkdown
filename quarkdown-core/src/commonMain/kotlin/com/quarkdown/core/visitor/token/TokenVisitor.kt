package com.quarkdown.core.visitor.token

/**
 * A visitor for [com.quarkdown.core.lexer.Token]s.
 * @param T output type of the `visit` methods
 */
interface TokenVisitor<T> :
    BlockTokenVisitor<T>,
    InlineTokenVisitor<T>
