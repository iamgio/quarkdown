package eu.iamgio.quarkdown.ast.iterator

import eu.iamgio.quarkdown.ast.NestableNode

/**
 * An iterator that runs through the nodes of an AST.
 */
interface AstIterator {
    /**
     * Runs the iterator from the given root node
     */
    fun run(root: NestableNode)
}
