package com.quarkdown.core.ast.dsl

import com.quarkdown.core.ast.Node

/**
 * A builder of a [Node] tree.
 * @see BlockAstBuilder
 * @see InlineAstBuilder
 * @see ListAstBuilder
 */
open class AstBuilder {
    /**
     * The tree that is being built.
     */
    protected val ast = mutableListOf<Node>()

    /**
     * Adds a node to the tree.
     * @param node node to add
     */
    fun node(node: Node) {
        ast.add(node)
    }

    /**
     * Adds [this] node to the tree. Shorthand for [node] (DSL syntactic sugar).
     * Usage: `+node`
     */
    operator fun Node.unaryPlus() = node(this)

    /**
     * Builds the tree.
     * @return the tree
     */
    fun build() = ast.toList()
}
