package com.quarkdown.core.ast.iterator

import com.quarkdown.core.ast.AstRoot

/**
 * An iterator that runs through the nodes of an AST.
 *
 * @param T result produced by [traverse]. Pure visitors that mutate context or fire hooks
 *          typically use [Unit]; rewriters that produce a transformed tree use [AstRoot].
 */
interface AstIterator<T> {
    /**
     * Runs the iterator from the given root node, traversing the node tree and visiting each node.
     * @param root root of the AST to traverse
     * @return the result of the traversal, whose semantics depend on the iterator
     */
    fun traverse(root: AstRoot): T
}
