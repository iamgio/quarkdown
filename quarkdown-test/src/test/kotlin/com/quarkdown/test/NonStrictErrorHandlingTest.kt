package com.quarkdown.test

import com.quarkdown.core.pipeline.error.BasePipelineErrorHandler
import com.quarkdown.test.util.execute
import org.junit.jupiter.api.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

/**
 * Tests for non-strict error handling, where errors are reported in the output instead of throwing exceptions.
 */
class NonStrictErrorHandlingTest {
    @Test
    fun `at binding time`() {
        execute(".sum {a} {3}", errorHandler = BasePipelineErrorHandler()) {
            assertEquals(
                "<div class=\"box error\">" +
                    "<header><h4>Error: sum</h4></header>" +
                    "<div class=\"box-content\"><p>" +
                    "Cannot call function sum" +
                    "<span class=\"inline-collapse\" data-full-text=\"(Number a, Number b)\" " +
                    "data-collapsed-text=\"(...)\" data-collapsed=\"false\">" +
                    "(Number a, Number b)" +
                    "</span>" +
                    " with arguments " +
                    "<span class=\"inline-collapse\" data-full-text=\"(a, 3)\" " +
                    "data-collapsed-text=\"(...)\" data-collapsed=\"false\">" +
                    "(a, 3)" +
                    "</span>: <br />" +
                    "<em>Not a numeric value: a</em>" +
                    "</p>" +
                    "<pre><code class=\"no-highlight nohljsln\">.sum {a} {3}</code></pre>" +
                    "</div></div>",
                it,
            )
        }
    }

    @Test
    fun `in nested expression`() {
        execute(".if {yes}\n\t.sum {a} {3}", errorHandler = BasePipelineErrorHandler()) {
            assertContains(it, "<h4>Error: sum</h4>")
            assertContains(it, ".sum {a} {3}")
        }
    }

    @Test
    fun `in nested expression, in nested content`() {
        execute(".if {yes}\n\t.row\n\t\t.sum {2} {1} {5}", errorHandler = BasePipelineErrorHandler()) {
            assertContains(it, "<h4>Error: sum</h4>")
        }
    }

    @Test
    fun `invalid value reference`() {
        execute(".if {yes}\n\t.column alignment:{x}\n\t\tHi", errorHandler = BasePipelineErrorHandler()) {
            assertContains(it, "<header><h4>Error: column</h4></header>")
            assertContains(it, "Cannot call function column")
            assertContains(it, "No such element 'x' among values [")
            assertContains(it, "<pre><code class=\"no-highlight nohljsln\">.column alignment:{x}\n\tHi</code></pre>")
        }
    }

    @Test
    fun `at runtime`() {
        execute(".csv {nonexistent}", errorHandler = BasePipelineErrorHandler()) {
            assertContains(it, "<h4>Error: csv</h4>")
            assertContains(it, "Cannot call function csv")
            assertContains(it, "with arguments")
            assertContains(it, "nonexistent does not exist")
            assertContains(it, ".csv {nonexistent}</code></pre>")
        }
    }

    @Test
    fun `multiple layers of nesting`() {
        execute(
            """
            .container
                a

                .row alignment:{center}
                    .if {invalid}
                        b
            """.trimIndent(),
            errorHandler = BasePipelineErrorHandler(),
        ) {
            assertContains(it, "<div class=\"container\"><p>a</p>")
            assertContains(it, "<h4>Error: if</h4>")
            assertContains(it, "Cannot call function if")
            assertContains(it, ".if {invalid}")
        }
    }

    @Test
    fun `long source snippet should be folded`() {
        execute(
            """
            .sum {1}
                1
                2
                3
                4
                5
                6
                7
                8
                9
                10
                11
            """.trimIndent(),
            errorHandler = BasePipelineErrorHandler(),
        ) {
            assertContains(it, "9\n... (2 more lines)")
        }
    }
}
