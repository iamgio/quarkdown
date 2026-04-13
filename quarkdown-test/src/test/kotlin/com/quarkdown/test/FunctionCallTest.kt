package com.quarkdown.test

import com.quarkdown.core.ast.attributes.presence.hasCode
import com.quarkdown.core.ast.attributes.presence.hasMath
import com.quarkdown.test.util.execute
import kotlin.test.Test
import kotlin.test.assertEquals
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
    fun `adjacent inline function calls`() {
        execute(".sum {1} {2} .sum {3} {4}") {
            assertEquals("<p>3 7</p>", it)
        }

        execute(".sum {1} {2}") {
            assertEquals("<p>3</p>", it)
        }

        execute(".sum {1} {2} result") {
            assertEquals("<p>3 result</p>", it)
        }

        execute(".sum {1} {2} abc .sum {3} {4}") {
            assertEquals("<p>3 abc 7</p>", it)
        }

        execute(".sum {1} {2} abc {}") {
            assertEquals("<p>3 abc {}</p>", it)
        }
    }

    @Test
    fun `wrapped block function call`() {
        execute("{.sum {1} {2}}") {
            assertEquals("<p>3</p>", it)
        }

        execute(
            """
            .if {yes}
                {.sum {1} {2}}jkl
            """.trimIndent(),
        ) {
            assertEquals("<p>3jkl</p>", it)
        }
    }

    @Test
    fun `line continuation`() {
        // Basic: inline args across lines.
        execute(".sum {3} \\\n{4}") {
            assertEquals("<p>7</p>", it)
        }

        // Named args across lines.
        execute(".multiply {3} \\\nby:{6}") {
            assertEquals("<p>18</p>", it)
        }

        // Multiple continuations.
        execute(
            """
            .sum {.sum {1} {2}} \
            {4}
            """.trimIndent(),
        ) {
            assertEquals("<p>7</p>", it)
        }

        // Continuation + body argument.
        execute(
            """
            .code \
            lang:{txt}
                hello
            """.trimIndent(),
        ) {
            assertEquals(
                "<pre><code class=\"language-txt\">hello</code></pre>",
                it,
            )
        }

        // Trailing content after args on a continuation line is inline, not block.
        execute(
            """
            .sum {1} \
                 {2} hello
            """.trimIndent(),
        ) {
            assertEquals("<p>3 hello</p>", it)
        }
    }

    @Test
    fun `wrapped inline function call (loose)`() {
        execute("hello {.sum {1} {2}} hello") {
            assertEquals("<p>hello 3 hello</p>", it)
        }
    }

    @Test
    fun `wrapped inline function call (tight)`() {
        execute("hello{.sum {1} {2}}hello") {
            assertEquals("<p>hello3hello</p>", it)
        }
    }

    @Test
    fun `escaped wrap`() {
        execute("hello\\{.sum {1} {2}}hello") {
            assertEquals("<p>hello{3}hello</p>", it)
        }
    }

    @Test
    fun `malformed wrap`() {
        execute("{.sum {1} {2} .sum {1} {2}}") {
            assertEquals("<p>{3 3}</p>", it)
        }

        execute(
            """
            .if {yes}
                abc{.uppercase {def} .uppercase {ghi}}jkl
            """.trimIndent(),
        ) {
            assertEquals("<p>abc{DEF GHI}jkl</p>", it)
        }
    }

    @Test
    fun `incomplete wrap`() {
        execute("hello{.sum {1} {2}") {
            assertEquals("<p>hello{3</p>", it)
        }
    }
}
