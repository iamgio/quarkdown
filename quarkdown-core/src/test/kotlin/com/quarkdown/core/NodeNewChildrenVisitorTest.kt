package com.quarkdown.core

import com.quarkdown.core.ast.AstRoot
import com.quarkdown.core.ast.Node
import com.quarkdown.core.ast.base.block.Heading
import com.quarkdown.core.ast.base.block.Newline
import com.quarkdown.core.ast.base.inline.Emphasis
import com.quarkdown.core.ast.base.inline.Text
import com.quarkdown.core.ast.quarkdown.block.Container
import com.quarkdown.core.ast.rewriter.withChildren
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame

/**
 * Tests for [com.quarkdown.core.ast.rewriter.NodeNewChildrenVisitor]
 * and its public [Node.withChildren] entry point.
 */
class NodeNewChildrenVisitorTest {
    private val replacement: List<Node> = listOf(Text("replacement"))

    @Test
    fun `replaces children of AstRoot`() {
        val original = AstRoot(listOf(Text("original")))
        val updated = assertIs<AstRoot>(original.withChildren(replacement))
        assertEquals(replacement, updated.children)
    }

    @Test
    fun `replaces text of a text node`() {
        val original = Emphasis(listOf(Text("original")))
        val updated = assertIs<Emphasis>(original.withChildren(replacement))
        assertEquals(replacement, updated.text)
    }

    @Test
    fun `preserves non-children constructor parameters`() {
        val original =
            Container(
                fullWidth = true,
                className = "highlight",
                children = listOf(Text("original")),
            )
        val updated = assertIs<Container>(original.withChildren(replacement))
        assertEquals(replacement, updated.children)
        assertEquals(true, updated.fullWidth)
        assertEquals("highlight", updated.className)
    }

    @Test
    fun `preserves non-text fields of Heading`() {
        val original =
            Heading(
                depth = 3,
                text = listOf(Text("original")),
                customId = "intro",
                canBreakPage = false,
            )
        val updated = assertIs<Heading>(original.withChildren(replacement))
        assertEquals(replacement, updated.text)
        assertEquals(3, updated.depth)
        assertEquals("intro", updated.customId)
        assertEquals(false, updated.canBreakPage)
    }

    @Test
    fun `leaf nodes are returned unchanged`() {
        assertSame(Newline, Newline.withChildren(replacement))
    }
}
