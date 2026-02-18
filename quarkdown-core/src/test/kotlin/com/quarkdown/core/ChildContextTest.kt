package com.quarkdown.core

import com.quarkdown.core.context.MutableContext
import com.quarkdown.core.context.SharedContext
import kotlin.test.Test
import kotlin.test.assertNull
import kotlin.test.assertSame

/**
 * Tests for [com.quarkdown.core.context.ChildContext].
 */
class ChildContextTest {
    @Test
    fun `has correct parent`() {
        val parent = MutableContext()
        val child = SharedContext(parent)
        assertSame(parent, child.parent)
    }

    @Test
    fun `root returns parent when single level`() {
        val parent = MutableContext()
        val child = SharedContext(parent)
        assertSame(parent, child.root)
    }

    @Test
    fun `root returns topmost context in deep hierarchy`() {
        val root = MutableContext()
        val level1 = SharedContext(root)
        val level2 = SharedContext(level1)
        val level3 = SharedContext(level2)

        assertSame(root, level1.root)
        assertSame(root, level2.root)
        assertSame(root, level3.root)
    }

    @Test
    fun `lastParentOrNull returns null when no context matches`() {
        val parent = MutableContext()
        val child = SharedContext(parent)
        assertNull(child.lastParentOrNull { false })
    }

    @Test
    fun `lastParentOrNull returns this when only child matches`() {
        val parent = MutableContext()
        val child = SharedContext(parent)
        assertSame(child, child.lastParentOrNull { it is SharedContext })
    }

    @Test
    fun `lastParentOrNull returns parent when only parent matches`() {
        val parent = MutableContext()
        val child = SharedContext(parent)
        assertSame(parent, child.lastParentOrNull { it !is SharedContext })
    }

    @Test
    fun `lastParentOrNull returns topmost matching context in hierarchy`() {
        val root = MutableContext()
        val level1 = SharedContext(root)
        val level2 = SharedContext(level1)
        val level3 = SharedContext(level2)

        // All SharedContexts match, so the last (topmost) is level1
        assertSame(level1, level3.lastParentOrNull { it is SharedContext })
    }
}
