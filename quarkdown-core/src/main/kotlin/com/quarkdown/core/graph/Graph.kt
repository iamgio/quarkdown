package com.quarkdown.core.graph

/**
 * A generic, immutable graph structure.
 * @param T the type of vertices in the graph
 */
interface Graph<T> {
    /**
     * The set of vertices in the graph.
     */
    val vertices: Set<T>

    /**
     * The set of edges in the graph, represented as pairs of vertices.
     */
    val edges: Set<Pair<T, T>>

    /**
     * @param vertex the vertex whose neighbors are to be retrieved.
     * @return a sequence of neighboring vertices for the specified vertex.
     */
    fun getNeighbors(vertex: T): Sequence<T>

    /**
     * Adds a vertex to the graph.
     * @param value the value of the vertex to add
     * @return a new graph instance with the vertex added
     */
    fun addVertex(value: T): Graph<T>

    /**
     * Adds an edge from one vertex to another.
     * @param from the source vertex
     * @param to the destination vertex
     * @return a new graph instance with the edge added
     */
    fun addEdge(
        from: T,
        to: T,
    ): Graph<T>

    /**
     * @see addEdge
     */
    fun addEdge(pair: Pair<T, T>): Graph<T> = addEdge(from = pair.first, to = pair.second)
}
