package com.quarkdown.core

import com.quarkdown.core.graph.DirectedGraph
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertTrue

/**
 *
 */
class GraphTest {
    private val graph = DirectedGraph<Int>()

    @Test
    fun empty() {
        assertTrue(graph.vertices.isEmpty())
    }

    @Test
    fun addVertex() {
        val new = graph.addVertex(1)
        assertContains(new.vertices, 1)
        assertTrue(new.getNeighbors(1).none())
        assertTrue(graph.vertices.isEmpty())
    }

    @Test
    fun addEdge() {
        val new =
            graph
                .addVertex(1)
                .addVertex(2)
                .addEdge(1, 2)

        assertContains(new.vertices, 1)
        assertContains(new.vertices, 2)
        assertTrue(new.getNeighbors(1).contains(2))
        assertTrue(new.getNeighbors(2).none())
    }
}
