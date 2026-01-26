package com.quarkdown.core.graph

import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.persistentSetOf

/**
 * An immutable directed graph using persistent data structures.
 * Updates return new instances that share structure with the original,
 * providing O(log n) time complexity per operation.
 *
 * @param T the type of vertices in the graph
 * @param vertices the set of vertices in the graph
 * @param adjacency a map from each vertex to its set of neighbors (outgoing edges)
 */
data class PersistentDirectedGraph<T>(
    override val vertices: PersistentSet<T> = persistentSetOf(),
    private val adjacency: PersistentMap<T, PersistentSet<T>> = persistentMapOf(),
) : Graph<T> {
    override val edges: Set<Pair<T, T>>
        get() = adjacency.flatMap { (from, neighbors) -> neighbors.map { from to it } }.toSet()

    override fun getNeighbors(vertex: T): Sequence<T> = adjacency[vertex]?.asSequence() ?: emptySequence()

    override fun addVertex(value: T): PersistentDirectedGraph<T> = copy(vertices = vertices.add(value))

    override fun addEdge(
        from: T,
        to: T,
    ): PersistentDirectedGraph<T> {
        val currentNeighbors = adjacency[from] ?: persistentSetOf()
        return copy(adjacency = adjacency.put(from, currentNeighbors.add(to)))
    }

    override fun addVertexAndEdge(
        vertex: T,
        edgeFrom: T,
        edgeTo: T,
    ): PersistentDirectedGraph<T> {
        val currentNeighbors = adjacency[edgeFrom] ?: persistentSetOf()
        return copy(
            vertices = vertices.add(vertex),
            adjacency = adjacency.put(edgeFrom, currentNeighbors.add(edgeTo)),
        )
    }
}
