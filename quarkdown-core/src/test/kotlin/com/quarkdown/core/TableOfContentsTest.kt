package com.quarkdown.core

import com.quarkdown.core.ast.base.block.Heading
import com.quarkdown.core.ast.base.inline.Text
import com.quarkdown.core.context.toc.TableOfContents
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for [TableOfContents] generation.
 */
class TableOfContentsTest {
    private fun heading(level: Int, text: String) = Heading(level, listOf(Text(text)))

    private fun generateToc(vararg headings: Heading) =
        TableOfContents.generate(headings.asSequence())

    private fun assertHeadingText(expected: String, item: TableOfContents.Item) {
        assertNodeEquals(Text(expected), item.text.first())
    }

    @Test
    fun `empty headings produce empty toc`() {
        val toc = generateToc()
        assertTrue(toc.items.isEmpty())
    }

    @Test
    fun `single heading`() {
        val toc = generateToc(heading(1, "Title"))

        assertEquals(1, toc.items.size)
        assertHeadingText("Title", toc.items[0])
        assertTrue(toc.items[0].subItems.isEmpty())
    }

    @Test
    fun `multiple h1 headings are siblings`() {
        val toc = generateToc(
            heading(1, "First"),
            heading(1, "Second"),
            heading(1, "Third"),
        )

        assertEquals(3, toc.items.size)
        assertHeadingText("First", toc.items[0])
        assertHeadingText("Second", toc.items[1])
        assertHeadingText("Third", toc.items[2])
    }

    @Test
    fun `h2 is nested under h1`() {
        val toc = generateToc(
            heading(1, "Parent"),
            heading(2, "Child"),
        )

        assertEquals(1, toc.items.size)
        assertEquals(1, toc.items[0].subItems.size)
        assertHeadingText("Parent", toc.items[0])
        assertHeadingText("Child", toc.items[0].subItems[0])
    }

    @Test
    fun `multiple h2 under same h1`() {
        val toc = generateToc(
            heading(1, "Parent"),
            heading(2, "Child 1"),
            heading(2, "Child 2"),
            heading(2, "Child 3"),
        )

        assertEquals(1, toc.items.size)
        assertEquals(3, toc.items[0].subItems.size)
        assertHeadingText("Child 1", toc.items[0].subItems[0])
        assertHeadingText("Child 2", toc.items[0].subItems[1])
        assertHeadingText("Child 3", toc.items[0].subItems[2])
    }

    @Test
    fun `h3 is nested under h2`() {
        val toc = generateToc(
            heading(1, "H1"),
            heading(2, "H2"),
            heading(3, "H3"),
        )

        assertEquals(1, toc.items.size)
        assertEquals(1, toc.items[0].subItems.size)
        assertEquals(1, toc.items[0].subItems[0].subItems.size)
        assertHeadingText("H3", toc.items[0].subItems[0].subItems[0])
    }

    @Test
    fun `complex nested hierarchy`() {
        val toc = generateToc(
            heading(1, "ABC"),
            heading(2, "DEF"),
            heading(2, "GHI"),
            heading(3, "JKL"),
            heading(2, "MNO"),
            heading(1, "PQR"),
        )

        assertEquals(2, toc.items.size)
        assertEquals(3, toc.items[0].subItems.size)
        assertEquals(1, toc.items[0].subItems[1].subItems.size)

        assertHeadingText("ABC", toc.items[0])
        assertHeadingText("DEF", toc.items[0].subItems[0])
        assertHeadingText("GHI", toc.items[0].subItems[1])
        assertHeadingText("JKL", toc.items[0].subItems[1].subItems[0])
        assertHeadingText("MNO", toc.items[0].subItems[2])
        assertHeadingText("PQR", toc.items[1])
    }

    @Test
    fun `level skip - h1 to h3 directly`() {
        val toc = generateToc(
            heading(1, "ABC"),
            heading(3, "DEF"),
            heading(2, "GHI"),
        )

        assertEquals(1, toc.items.size)
        assertEquals(2, toc.items[0].subItems.size)

        assertHeadingText("ABC", toc.items[0])
        assertHeadingText("DEF", toc.items[0].subItems[0])
        assertHeadingText("GHI", toc.items[0].subItems[1])
    }

    @Test
    fun `new h1 resets nesting`() {
        val toc = generateToc(
            heading(1, "First Section"),
            heading(2, "Subsection"),
            heading(3, "Deep"),
            heading(1, "Second Section"),
            heading(2, "Another Subsection"),
        )

        assertEquals(2, toc.items.size)
        assertEquals(1, toc.items[0].subItems.size)
        assertEquals(1, toc.items[0].subItems[0].subItems.size)
        assertEquals(1, toc.items[1].subItems.size)

        assertHeadingText("First Section", toc.items[0])
        assertHeadingText("Second Section", toc.items[1])
        assertHeadingText("Another Subsection", toc.items[1].subItems[0])
    }

    @Test
    fun `deep nesting up to h6`() {
        val toc = generateToc(
            heading(1, "Level 1"),
            heading(2, "Level 2"),
            heading(3, "Level 3"),
            heading(4, "Level 4"),
            heading(5, "Level 5"),
            heading(6, "Level 6"),
        )

        assertEquals(1, toc.items.size)

        var current = toc.items[0]
        assertHeadingText("Level 1", current)

        for (level in 2..6) {
            assertEquals(1, current.subItems.size)
            current = current.subItems[0]
            assertHeadingText("Level $level", current)
        }

        assertTrue(current.subItems.isEmpty())
    }

    @Test
    fun `alternating levels`() {
        val toc = generateToc(
            heading(1, "H1-A"),
            heading(2, "H2-A"),
            heading(1, "H1-B"),
            heading(2, "H2-B"),
            heading(1, "H1-C"),
        )

        assertEquals(3, toc.items.size)
        assertEquals(1, toc.items[0].subItems.size)
        assertEquals(1, toc.items[1].subItems.size)
        assertEquals(0, toc.items[2].subItems.size)

        assertHeadingText("H1-A", toc.items[0])
        assertHeadingText("H2-A", toc.items[0].subItems[0])
        assertHeadingText("H1-B", toc.items[1])
        assertHeadingText("H2-B", toc.items[1].subItems[0])
        assertHeadingText("H1-C", toc.items[2])
    }

    @Test
    fun `going back up multiple levels`() {
        val toc = generateToc(
            heading(1, "H1"),
            heading(2, "H2"),
            heading(3, "H3"),
            heading(4, "H4"),
            heading(2, "Back to H2"),
        )

        assertEquals(1, toc.items.size)
        assertEquals(2, toc.items[0].subItems.size)

        assertHeadingText("H2", toc.items[0].subItems[0])
        assertHeadingText("Back to H2", toc.items[0].subItems[1])
        assertEquals(1, toc.items[0].subItems[0].subItems.size)
        assertEquals(0, toc.items[0].subItems[1].subItems.size)
    }

    @Test
    fun `headings starting at level 2`() {
        val toc = generateToc(
            heading(2, "ABC"),
            heading(3, "DEF"),
            heading(2, "GHI"),
            heading(1, "JKL"),
        )

        assertEquals(3, toc.items.size)
        assertEquals(1, toc.items[0].subItems.size)

        assertHeadingText("ABC", toc.items[0])
        assertHeadingText("DEF", toc.items[0].subItems[0])
        assertHeadingText("GHI", toc.items[1])
        assertHeadingText("JKL", toc.items[2])
    }

    @Test
    fun `multiple branches at same level`() {
        val toc = generateToc(
            heading(1, "Root"),
            heading(2, "Branch A"),
            heading(3, "Leaf A1"),
            heading(3, "Leaf A2"),
            heading(2, "Branch B"),
            heading(3, "Leaf B1"),
        )

        assertEquals(1, toc.items.size)
        assertEquals(2, toc.items[0].subItems.size)

        val branchA = toc.items[0].subItems[0]
        val branchB = toc.items[0].subItems[1]

        assertHeadingText("Branch A", branchA)
        assertEquals(2, branchA.subItems.size)
        assertHeadingText("Leaf A1", branchA.subItems[0])
        assertHeadingText("Leaf A2", branchA.subItems[1])

        assertHeadingText("Branch B", branchB)
        assertEquals(1, branchB.subItems.size)
        assertHeadingText("Leaf B1", branchB.subItems[0])
    }
}
