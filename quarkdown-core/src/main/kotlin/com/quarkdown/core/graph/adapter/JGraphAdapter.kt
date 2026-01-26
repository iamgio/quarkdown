package com.quarkdown.core.graph.adapter

import com.quarkdown.core.graph.Graph
import org.jgrapht.Graphs
import org.jgrapht.graph.DefaultDirectedGraph
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.Graph as JGraph

/**
 * A [Graph] implementation that uses JGraphT as the underlying data structure.
 * @param emptyGraphSupplier a supplier function that provides an empty JGraph
 * @param graph the underlying JGraph instance
 * @param T the type of vertices in the graph
 */
internal class JGraphAdapter<T>(
    private val emptyGraphSupplier: () -> JGraph<T, DefaultEdge>,
    private val graph: JGraph<T, DefaultEdge> = emptyGraphSupplier(),
) : Graph<T> {
    override val vertices: Set<T>
        get() = graph.vertexSet()

    override val edges: Set<Pair<T, T>>
        get() =
            graph
                .edgeSet()
                .map { edge -> graph.getEdgeSource(edge) to graph.getEdgeTarget(edge) }
                .toSet()

    override fun getNeighbors(vertex: T): Sequence<T> =
        graph
            .outgoingEdgesOf(vertex)
            .asSequence()
            .map(graph::getEdgeTarget)

    override fun addVertex(value: T): Graph<T> =
        copyGraph(graph)
            .apply { addVertex(value) }
            .let { JGraphAdapter(emptyGraphSupplier, it) }

    override fun addEdge(
        from: T,
        to: T,
    ): Graph<T> =
        copyGraph(graph)
            .apply { addEdge(from, to) }
            .let { JGraphAdapter(emptyGraphSupplier, it) }

    override fun addVertexAndEdge(
        vertex: T,
        edgeFrom: T,
        edgeTo: T,
    ): Graph<T> =
        copyGraph(graph)
            .apply {
                addVertex(vertex)
                addEdge(edgeFrom, edgeTo)
            }.let { JGraphAdapter(emptyGraphSupplier, it) }

    private fun copyGraph(original: JGraph<T, DefaultEdge>): JGraph<T, DefaultEdge> =
        emptyGraphSupplier().apply { Graphs.addGraph(this, original) }

    companion object {
        fun <T> directed(): JGraph<T, DefaultEdge> = DefaultDirectedGraph(DefaultEdge::class.java)
    }
}
