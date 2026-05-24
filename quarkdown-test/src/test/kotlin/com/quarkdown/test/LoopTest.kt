package com.quarkdown.test

import com.quarkdown.test.util.execute
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for the looping statements `.foreach` and `.repeat`,
 * covering range syntax, named-range syntax and nesting.
 */
class LoopTest {
    @Test
    fun `foreach over implicit range starting at one`() {
        execute(
            """
            .foreach {..3}
                **N:** .1
            """.trimIndent(),
        ) {
            assertEquals(
                "<p><strong>N:</strong> 1</p>" +
                    "<p><strong>N:</strong> 2</p>" +
                    "<p><strong>N:</strong> 3</p>",
                it,
            )
        }
    }

    @Test
    fun `foreach over explicit range`() {
        execute(
            """
            .foreach {2..4}
                **N:** .1
            """.trimIndent(),
        ) {
            assertEquals(
                "<p><strong>N:</strong> 2</p>" +
                    "<p><strong>N:</strong> 3</p>" +
                    "<p><strong>N:</strong> 4</p>",
                it,
            )
        }
    }

    @Test
    fun `foreach over a range built via the range function`() {
        execute(
            """
            .foreach {.range from:{2} to:{4}}
                **N:** .1
            """.trimIndent(),
        ) {
            assertEquals(
                "<p><strong>N:</strong> 2</p>" +
                    "<p><strong>N:</strong> 3</p>" +
                    "<p><strong>N:</strong> 4</p>",
                it,
            )
        }
    }

    @Test
    fun `foreach with explicit named parameter`() {
        execute(
            """
            ## Title
            .foreach {..2}
                n:
                Hi .n
            """.trimIndent(),
        ) {
            assertEquals("<h2>Title</h2><p>Hi 1</p><p>Hi 2</p>", it)
        }
    }

    @Test
    fun `nested foreach exposes inner index`() {
        execute(
            """
            .foreach {.range to:{.sum {1} {1}}}
                ## Hello .1
                .foreach {..1}
                    **Hi**!
            """.trimIndent(),
        ) {
            assertEquals(
                "<h2>Hello 1</h2><p><strong>Hi</strong>!</p>" +
                    "<h2>Hello 2</h2><p><strong>Hi</strong>!</p>",
                it,
            )
        }
    }

    @Test
    fun `repeat is equivalent to foreach over an implicit range`() {
        execute(
            """
            .repeat {2}
                ## Hello .1
                .repeat {1}
                    **Hi**!
            """.trimIndent(),
        ) {
            assertEquals(
                "<h2>Hello 1</h2><p><strong>Hi</strong>!</p>" +
                    "<h2>Hello 2</h2><p><strong>Hi</strong>!</p>",
                it,
            )
        }
    }

    @Test
    fun `deeply nested foreach with trailing siblings`() {
        execute(
            """
            .foreach {..2}
                .foreach {.range to:{2}}
                    .foreach {..2}
                        ## Title 2
                    # Title 1

                Some text
            ### Title 3
            """.trimIndent(),
        ) {
            val innerBlock =
                "<h2>Title 2</h2><h2>Title 2</h2>" +
                    "<h1 class=\"page-break\">Title 1</h1>"
            val outerIteration = innerBlock.repeat(2) + "<p>Some text</p>"
            assertEquals(
                outerIteration.repeat(2) + "<h3>Title 3</h3>",
                it,
            )
        }
    }

    @Test
    fun `foreach over a single-element range still iterates once`() {
        execute(
            """
            .foreach {3..3}
                hit .1
            """.trimIndent(),
        ) {
            assertEquals("<p>hit 3</p>", it)
        }
    }

    @Test
    fun `repeat zero produces no output`() {
        execute(
            """
            before

            .repeat {0}
                inside

            after
            """.trimIndent(),
        ) {
            assertEquals("<p>before</p><p>after</p>", it)
        }
    }

    @Test
    fun `repeat with index used inside a custom layout function`() {
        execute(
            """
            .function {greet}
                n:
                Hello .n!

            .repeat {3}
                n:
                .greet {.n}
            """.trimIndent(),
        ) {
            assertEquals("<p>Hello 1!</p><p>Hello 2!</p><p>Hello 3!</p>", it)
        }
    }
}
