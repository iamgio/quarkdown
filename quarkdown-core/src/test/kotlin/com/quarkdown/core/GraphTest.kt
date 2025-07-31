package com.quarkdown.core

import com.quarkdown.core.graph.DirectedGraph
import com.quarkdown.core.graph.VisitableOnceGraph
import com.quarkdown.core.graph.visitableOnce
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for the graph data structure.
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

    @Test
    fun getNeighbors() {
        val new =
            graph
                .addVertex(1)
                .addVertex(2)
                .addVertex(3)
                .addEdge(1, 2)
                .addEdge(2, 1)
                .addEdge(2, 3)

        assertEquals(setOf(2), new.getNeighbors(1).toSet())
        assertEquals(setOf(1, 3), new.getNeighbors(2).toSet())
        assertTrue(new.getNeighbors(3).none())
    }

    @Test
    fun `visitable once`() {
        var visitableOnce =
            graph.visitableOnce
                .addVertex(1)
                .addVertex(2)
                .addVertex(3)
                .addEdge(1, 2)
                .addEdge(2, 1)
                .addEdge(2, 3)
                .addEdge(3, 1)

        val update = { updated: VisitableOnceGraph<Int> ->
            visitableOnce = updated
        }

        assertEquals(setOf(2), visitableOnce.visitNeighbors(1, update))
        assertEquals(emptySet(), visitableOnce.visitNeighbors(1, update)) // 2 is already visited
        assertEquals(setOf(1), visitableOnce.visitNeighbors(3, update))
        assertEquals(setOf(3), visitableOnce.visitNeighbors(2, update)) // 1 is already visited
    }
}
