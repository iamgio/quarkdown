package com.quarkdown.core.ast

import com.quarkdown.core.util.mapParallel
import com.quarkdown.core.visitor.node.NodeVisitor

/**
 * A node of the abstract syntax tree - can be either a block or an inline element.
 */
interface Node {
    /**
     * Accepts a visitor.
     * @param T output type of the visitor
     * @return output of the visit operation
     */
    fun <T> accept(visitor: NodeVisitor<T>): T
}

/**
 * A node that may contain a variable number of nested nodes as children.
 */
interface NestableNode : Node {
    val children: List<Node>
}

/**
 * A node that contains a single child node.
 * @param T type of the child node
 */
interface SingleChildNestableNode<T : Node> : NestableNode {
    /**
     * The single child node.
     */
    val child: T

    /**
     * A singleton list containing [child].
     */
    override val children: List<Node>
        get() = listOf(child)
}

/**
 * Accepts a visitor for each node sequentially.
 * @param visitor the visitor to accept
 * @return the list of results from each visit, preserving order
 */
fun <T> List<Node>.acceptAll(visitor: NodeVisitor<T>): List<T> = map { it.accept(visitor) }

/**
 * Accepts a visitor for each node, executing visits in parallel when beneficial.
 * Falls back to sequential execution for small lists.
 * @param visitor the visitor to accept
 * @param minItems minimum number of nodes required for parallel execution
 * @return the list of results from each visit, preserving order
 * @see mapParallel
 */
fun <T> List<Node>.parallelAcceptAll(
    visitor: NodeVisitor<T>,
    minItems: Int = 50,
): List<T> = mapParallel(minItems) { it.accept(visitor) }
