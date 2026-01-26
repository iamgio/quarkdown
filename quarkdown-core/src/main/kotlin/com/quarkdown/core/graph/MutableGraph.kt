package com.quarkdown.core.graph

/**
 * A mutable [Graph] that allows in-place modifications.
 * Mutation methods modify the graph in place and return `this` for method chaining.
 * @param T the type of vertices in the graph
 */
interface MutableGraph<T> : Graph<T> {
    override fun addVertex(value: T): MutableGraph<T>

    override fun addEdge(
        from: T,
        to: T,
    ): MutableGraph<T>

    override fun addEdge(pair: Pair<T, T>): MutableGraph<T> = addEdge(from = pair.first, to = pair.second)

    override fun addVertexAndEdge(
        vertex: T,
        edgeFrom: T,
        edgeTo: T,
    ): MutableGraph<T>
}
