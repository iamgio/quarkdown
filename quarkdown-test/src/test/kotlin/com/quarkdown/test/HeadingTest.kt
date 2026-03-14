package com.quarkdown.test

import com.quarkdown.core.function.error.FunctionCallRuntimeException
import com.quarkdown.test.util.DEFAULT_OPTIONS
import com.quarkdown.test.util.execute
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

/**
 * Tests for headings, both Markdown-based and via the `.heading` primitive function.
 */
class HeadingTest {
    @Test
    fun `markdown heading`() {
        execute("# Title") {
            assertEquals("<h1 class=\"page-break\">Title</h1>", it)
        }

        execute("## Ti*tl*e") {
            assertEquals("<h2>Ti<em>tl</em>e</h2>", it)
        }

        execute("#### .sum {3} {2}") {
            assertEquals("<h4>5</h4>", it)
        }

        execute("###### .text {Hello, **world**} size:{tiny}") {
            assertEquals("<h6><span class=\"size-tiny\">Hello, <strong>world</strong></span></h6>", it)
        }
    }

    @Test
    fun `decorative heading`() {
        execute("#! Title") {
            assertEquals("<h1 data-decorative=\"\">Title</h1>", it)
        }
    }

    @Test
    fun `default id`() {
        execute(
            "## Title",
            options = DEFAULT_OPTIONS.copy(enableAutomaticIdentifiers = true),
        ) {
            assertEquals("<h2 id=\"title\">Title</h2>", it)
        }
    }

    @Test
    fun `custom id`() {
        execute("## Title {#custom-id}") {
            assertEquals("<h2 id=\"custom-id\">Title</h2>", it)
        }
    }

    @Test
    fun `auto page break`() {
        execute(
            """
            .autopagebreak maxdepth:{4}
            ## A
            ### B
            ##### C
            """.trimIndent(),
        ) {
            assertEquals(
                "<h2 class=\"page-break\">A</h2>" +
                    "<h3 class=\"page-break\">B</h3>" +
                    "<h5>C</h5>",
                it,
            )
        }

        execute(
            """
            .noautopagebreak
            # A
            """.trimIndent(),
        ) {
            assertEquals("<h1>A</h1>", it)
        }
    }

    @Test
    fun `heading primitive`() {
        execute(".heading {Hello} depth:{1}") {
            assertEquals("<h1 class=\"page-break\">Hello</h1>", it)
        }

        execute(".heading {Hello} depth:{3}") {
            assertEquals("<h3>Hello</h3>", it)
        }
    }

    @Test
    fun `heading primitive with custom id`() {
        execute(".heading {Hello} depth:{2} ref:{my-id}") {
            assertEquals("<h2 id=\"my-id\">Hello</h2>", it)
        }
    }

    @Test
    fun `heading primitive unnumbered`() {
        execute(".heading {Hello} depth:{2} numbered:{no}") {
            assertEquals("<h2>Hello</h2>", it)
        }
    }

    @Test
    fun `heading primitive decorative`() {
        execute(".heading {Hello} depth:{2} numbered:{no} indexed:{no} breakpage:{no}") {
            assertEquals("<h2 data-decorative=\"\">Hello</h2>", it)
        }
    }

    @Test
    fun `heading primitive no page break`() {
        execute(".heading {Hello} depth:{1} breakpage:{no}") {
            assertEquals("<h1>Hello</h1>", it)
        }
    }

    @Test
    fun `heading primitive depth out of range`() {
        assertFailsWith<FunctionCallRuntimeException> {
            execute(".heading {Hello} depth:{0}") {}
        }.also { exception ->
            assertIs<IllegalArgumentException>(exception.cause)
        }

        assertFailsWith<FunctionCallRuntimeException> {
            execute(".heading {Hello} depth:{7}") {}
        }.also { exception ->
            assertIs<IllegalArgumentException>(exception.cause)
        }
    }
}
