package com.quarkdown.test

import com.quarkdown.rendering.plaintext.extension.plainText
import com.quarkdown.test.util.execute
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests that parallel rendering produces deterministic output identical to sequential rendering.
 * These tests exercise documents with enough nodes to trigger parallelism (50+ sibling nodes),
 * ensuring that order-sensitive rendering operations remain correct under concurrency.
 */
class ParallelRenderingTest {
    /**
     * A document with many top-level blocks that exceed the parallel threshold.
     * Verifies that sibling block rendering order is preserved.
     */
    @Test
    fun `many paragraphs`() {
        val source = (1..60).joinToString("\n\n") { "Paragraph $it with **bold** and *italic* text." }

        execute(source) { result ->
            val expected =
                (1..60).joinToString("") {
                    "<p>Paragraph $it with <strong>bold</strong> and <em>italic</em> text.</p>"
                }
            assertEquals(expected, result)
        }
    }

    /**
     * A document with headings and paragraphs interleaved, producing enough top-level nodes
     * to trigger parallelism. Tests that the block order is preserved.
     */
    @Test
    fun `interleaved headings and paragraphs`() {
        val source =
            (1..30).joinToString("\n\n") { i ->
                "## Section $i\n\nContent of section $i."
            }

        execute(source) { result ->
            val expected =
                (1..30).joinToString("") { i ->
                    "<h2>Section $i</h2><p>Content of section $i.</p>"
                }
            assertEquals(expected, result)
        }
    }

    /**
     * Plaintext rendering with many blocks.
     * Verifies that the plaintext renderer's parallel `visitAll` preserves order.
     */
    @Test
    fun `plaintext rendering order`() {
        val source = (1..60).joinToString("\n\n") { "Paragraph $it." }

        execute(
            source,
            renderer = { rendererFactory, ctx -> rendererFactory.plainText(ctx) },
        ) { result ->
            val expected = (1..60).joinToString("") { "Paragraph $it.\n\n" }
            assertEquals(expected, result)
        }
    }

    /**
     * Repeated runs of the same document to detect non-deterministic ordering.
     * If parallelism introduces randomness, at least one run out of many would produce different output.
     */
    @Test
    fun `repeated rendering is deterministic`() {
        val source =
            (1..30).joinToString("\n\n") { i ->
                "## Heading $i\n\nParagraph with **bold $i** and *italic $i*."
            }

        val results = mutableListOf<String>()
        repeat(10) {
            execute(source) { result -> results += result.toString() }
        }

        val first = results.first()
        results.forEachIndexed { index, result ->
            assertEquals(first, result, "Run $index produced different output")
        }
    }
}
