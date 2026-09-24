package com.quarkdown.core.visitor.token

/**
 * A general [TokenVisitor] that delegates its visiting operations to one of its members.
 * @param blockVisitor visitor of block tokens
 * @param inlineVisitor visitor of inline tokens
 * @param T output type of the `visit` methods
 */
class TokenVisitorAdapter<T>(
    blockVisitor: BlockTokenVisitor<T>,
    inlineVisitor: InlineTokenVisitor<T>,
) : TokenVisitor<T>,
    BlockTokenVisitor<T> by blockVisitor,
    InlineTokenVisitor<T> by inlineVisitor
