package com.quarkdown.core.graph

/**
 * An immutable [Graph] decorator that allows visiting each vertex only once.
 * @param T the type of vertices in the graph
 * @property graph the underlying graph structure
 * @property visited a set of vertices that have already been visited
 */
data class VisitableOnceGraph<T>(
    private val graph: Graph<T>,
    private val visited: Set<T> = emptySet(),
) : Graph<T> by graph {
    /**
     * Checks if the given vertex has been visited.
     * @param vertex the vertex to check
     * @return `true` if the vertex has been visited, `false` otherwise
     */
    private fun isVisited(vertex: T): Boolean = vertex in visited

    /**
     * Retrieves the unvisited neighbors of the specified vertex.
     * @param vertex the vertex whose unvisited neighbors are to be retrieved
     * @return a sequence of unvisited neighboring vertices
     */
    private fun getUnvisitedNeighbors(vertex: T): Sequence<T> = getNeighbors(vertex).filterNot(::isVisited)

    /**
     * Visits the unvisited neighbors of the specified vertex, marking each vertex as visited.
     * @param vertex the vertex whose neighbors are to be visited
     * @param update an action to perform after visiting the neighbors, which is supposed to update the graph state with the visited vertices
     * @return the yet unvisited neighboring vertices that were now visited
     */
    fun visitNeighbors(
        vertex: T,
        update: (VisitableOnceGraph<T>) -> Unit,
    ): Set<T> =
        getUnvisitedNeighbors(vertex)
            .toSet()
            .also { visitedVertices ->
                val updatedGraph =
                    VisitableOnceGraph(
                        graph = graph,
                        visited = visited + visitedVertices,
                    )
                update(updatedGraph)
            }

    override fun addVertex(value: T): VisitableOnceGraph<T> = copy(graph = graph.addVertex(value))

    override fun addEdge(
        from: T,
        to: T,
    ): VisitableOnceGraph<T> = copy(graph = graph.addEdge(from, to))

    override fun addEdge(pair: Pair<T, T>): VisitableOnceGraph<T> = copy(graph = graph.addEdge(pair))

    override fun addVertexAndEdge(
        vertex: T,
        edgeFrom: T,
        edgeTo: T,
    ): VisitableOnceGraph<T> = copy(graph = graph.addVertexAndEdge(vertex, edgeFrom, edgeTo))
}

/**
 * Converts a [Graph] into a [VisitableOnceGraph] which allows visiting each vertex only once.
 * @receiver the graph to convert
 * @return a [VisitableOnceGraph] wrapping the original graph, and no initially visited vertices
 */
val <T> Graph<T>.visitableOnce: VisitableOnceGraph<T>
    get() = VisitableOnceGraph(this)
