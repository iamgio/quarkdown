package eu.iamgio.quarkdown.test

import eu.iamgio.quarkdown.function.error.InvalidArgumentCountException
import eu.iamgio.quarkdown.function.error.InvalidFunctionCallException
import eu.iamgio.quarkdown.function.error.UnresolvedReferenceException
import eu.iamgio.quarkdown.pipeline.error.BasePipelineErrorHandler
import eu.iamgio.quarkdown.test.util.execute
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for function calls.
 */
class FunctionCallTest {
    @Test
    fun functions() {
        execute(".sum {3} {4}") {
            assertEquals("<p>7</p>", it)
        }

        execute(".multiply {3} by:{6}") {
            assertEquals("<p>18</p>", it)
        }

        execute(
            """
            .divide {
              .cos {.pi}
            } by:{
              .sin {
                1
              }
            }
            """.trimIndent(),
        ) {
            assertEquals("<p>-1.1883951</p>", it)
        }

        execute("$ 4 - 2 = $ .subtract {4} {2}") {
            assertEquals("<p>__QD_INLINE_MATH__$4 - 2 =\$__QD_INLINE_MATH__ 2</p>", it)
            assertTrue(attributes.hasMath)
        }

        execute("***result***: .sum {3} {.multiply {4} {2}}") {
            assertEquals("<p><em><strong>result</strong></em>: 11</p>", it)
            assertFalse(attributes.hasMath)
        }

        execute(".code\n    .read {code.txt}") {
            assertEquals(
                "<pre><code>Line 1${System.lineSeparator()}Line 2${System.lineSeparator()}${System.lineSeparator()}Line 3</code></pre>",
                it,
            )
            assertTrue(attributes.hasCode)
        }
    }

    @Test
    fun errors() {
        assertFailsWith<UnresolvedReferenceException> {
            execute(".nonexistant") {}
        }

        assertFailsWith<InvalidArgumentCountException> {
            execute(".sum {2}") {}
        }

        assertFailsWith<InvalidArgumentCountException> {
            execute(".sum {2} {5} {9}") {}
        }

        assertFailsWith<InvalidFunctionCallException> {
            execute(".sum {a} {3}") {}
        }

        assertFailsWith<InvalidFunctionCallException> {
            execute(".if {hello}\n\t.sum {2} {3} {1}") {}
        }

        assertFailsWith<InvalidFunctionCallException> {
            execute(".row alignment:{center} cross:{hello}\n\tHi") {}
        }

        // Non-strict error handling.

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
                    "<span class=\"inline-collapse\" data-full-text=\"(a, 3)\" data-collapsed-text=\"(...)\" data-collapsed=\"false\">" +
                    "(a, 3)" +
                    "</span>: <br />" +
                    "<em>Not a numeric value: a</em>" +
                    "</p></div></div>",
                it,
            )
        }

        execute(".if {yes}\n\t.sum {a} {3}", errorHandler = BasePipelineErrorHandler()) {
            assertContains(it, "<h4>Error: sum</h4>")
        }

        execute(".if {yes}\n\t.row\n\t\t.sum {2} {1} {5}", errorHandler = BasePipelineErrorHandler()) {
            assertContains(it, "<h4>Error: sum</h4>")
        }

        execute(".if {yes}\n\t.column alignment:{x}\n\t\tHi", errorHandler = BasePipelineErrorHandler()) {
            assertTrue(
                Regex(
                    ".+?<header><h4>Error: column</h4></header>" +
                        ".+?<p>" +
                        "Cannot call function column.+?No such element 'x' among values \\[.+?]" +
                        "</p>.+",
                ).matches(it),
            )
        }
    }
}
