package com.quarkdown.test

import com.quarkdown.core.ast.attributes.presence.hasCode
import com.quarkdown.core.ast.attributes.presence.hasMath
import com.quarkdown.core.function.error.InvalidArgumentCountException
import com.quarkdown.core.function.error.InvalidFunctionCallException
import com.quarkdown.core.function.error.ParameterAlreadyBoundException
import com.quarkdown.core.function.error.UnresolvedReferenceException
import com.quarkdown.core.pipeline.error.BasePipelineErrorHandler
import com.quarkdown.test.util.execute
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
            assertEquals("<p><formula>4 - 2 =</formula> 2</p>", it)
            assertTrue(attributes.hasMath)
        }

        execute("***result***: .sum {3} {.multiply {4} {2}}") {
            assertEquals("<p><em><strong>result</strong></em>: 11</p>", it)
            assertFalse(attributes.hasMath)
        }

        execute(".code\n    .read {code.txt}") {
            assertEquals(
                "<pre><code>Line 1\nLine 2\n\nLine 3</code></pre>",
                it,
            )
            assertTrue(attributes.hasCode)
        }
    }

    @Test
    fun `error unresolved reference`() {
        assertFailsWith<UnresolvedReferenceException> {
            execute(".nonexistant") {}
        }
    }

    @Test
    fun `error argument count`() {
        assertFailsWith<InvalidArgumentCountException> {
            execute(".sum {2}") {}
        }

        assertFailsWith<InvalidArgumentCountException> {
            execute(".sum {2} {5} {9}") {}
        }
    }

    @Test
    fun `error positional parameter already bound`() {
        assertFailsWith<ParameterAlreadyBoundException> {
            execute(".sum {2} a:{3}") {}
        }
    }

    @Test
    fun `error named parameter already bound`() {
        assertFailsWith<ParameterAlreadyBoundException> {
            execute(".sum a:{2} a:{3}") {}
        }
    }

    @Test
    fun `error argument type`() {
        assertFailsWith<InvalidFunctionCallException> {
            execute(".sum {a} {3}") {}
        }

        assertFailsWith<InvalidFunctionCallException> {
            execute(".sum {2} {.multiply {3} {a}}") {}
        }

        assertFailsWith<InvalidFunctionCallException> {
            execute(".if {hello}\n\t.sum {2} {3} {1}") {}
        }

        assertFailsWith<InvalidFunctionCallException> {
            execute(".row alignment:{center} cross:{hello}\n\tHi") {}
        }
    }

    @Test
    fun `error document type`() {
        assertFailsWith<InvalidFunctionCallException> {
            execute(".doctype {plain}\n.slides") {}
        }
    }

    @Test
    fun `non strict error handling`() {
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
