package com.quarkdown.test

import com.quarkdown.core.pipeline.error.BasePipelineErrorHandler
import com.quarkdown.test.util.execute
import kotlin.test.Test
import kotlin.test.assertContains
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
    fun `join without body joins the elements themselves`() {
        execute(".var {abc}\n  - A\n  - B\n  - C\n\n.abc::join") {
            assertEquals("<p>A, B, C</p>", it)
        }
        execute(".var {nums}\n  - 1\n  - 2\n  - 3\n\n.nums::join separator:{+}") {
            assertEquals("<p>1+2+3</p>", it)
        }
    }

    @Test
    fun `join with default separator`() {
        execute(".var {abc}\n  - A\n  - B\n  - C\n\n.abc::join\n    x:\n    .x") {
            assertEquals("<p>A, B, C</p>", it)
        }
    }

    @Test
    fun `join with custom separator and rich output`() {
        execute(".var {abc}\n  - A\n  - B\n  - C\n\n.abc::join separator:{-}\n    x:\n    **.x**") {
            assertEquals("<p><strong>A</strong>-<strong>B</strong>-<strong>C</strong></p>", it)
        }
    }

    @Test
    fun `join with rich separator`() {
        execute(".var {abc}\n  - A\n  - B\n  - C\n\n.abc::join separator:{**|**}\n    x:\n    .x") {
            assertEquals("<p>A<strong>|</strong>B<strong>|</strong>C</p>", it)
        }
    }

    @Test
    fun `join inline within a paragraph`() {
        execute(".var {abc}\n  - A\n  - B\n  - C\n\nLetters: .abc::join body:{x: .x}.") {
            assertEquals("<p>Letters: A, B, C.</p>", it)
        }
    }

    @Test
    fun `join with indices destructured in body`() {
        execute(
            """
            .var {abc}
              - A
              - B
              - C

            .abc::withindices::join
                index letter:
                .index::text script:{sup} **.letter**
            """.trimIndent(),
        ) {
            assertEquals(
                "<p><sup>1</sup> <strong>A</strong>, <sup>2</sup> <strong>B</strong>, <sup>3</sup> <strong>C</strong></p>",
                it,
            )
        }
    }

    @Test
    fun `join of multi-line bodies with declarations stays inline`() {
        execute(
            """
            .var {names}
              - Gio
              - Alice

            .var {info}
                .names::foreach
                    name:
                    .var {x} {1}

                    .name .x

            .info::join
            """.trimIndent(),
        ) {
            assertEquals("<p>Gio 1, Alice 1</p>", it)
        }
    }

    @Test
    fun `join of multi-line bodies with node results stays inline`() {
        execute(
            """
            .var {names}
              - Gio
              - Alice

            .var {meta}
              .dictionary
                - Gio: 1
                - Alice: 2

            .var {info}
                .names::foreach
                    name:
                    .var {entries} {.meta::withindices::findall {index entry: .entry::first::equals {.name}}}

                    .name .entries::join body:{.1::first}

            .info::join
            """.trimIndent(),
        ) {
            assertEquals("<p>Gio 1, Alice 2</p>", it)
        }
    }

    @Test
    fun `join of bodies with a failing call keeps the error`() {
        execute(
            """
            .var {names}
              - Gio

            .var {info}
                .names::foreach
                    name:
                    .var {x} {1}

                    .name .undefinedfunction

            .info::join
            """.trimIndent(),
            errorHandler = BasePipelineErrorHandler(),
        ) {
            assertContains(it, "Error")
            assertContains(it, "undefinedfunction")
        }
    }

    @Test
    fun `join of empty collection produces nothing`() {
        execute(".range from:{1} to:{0}::join body:{x: .x}") {
            assertEquals("", it)
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
                    "<h1>Title 1</h1>"
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
