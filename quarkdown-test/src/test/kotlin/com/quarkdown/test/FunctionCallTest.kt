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

    @Test
    fun `identity function`() {
        execute(".{hello}") {
            assertEquals("<p>hello</p>", it)
        }
    }

    @Test
    fun `malformed nameless function does not parse`() {
        execute(".") { assertEquals("<p>.</p>", it) }
        execute("abc . def") { assertEquals("<p>abc . def</p>", it) }
        execute(". {def}") { assertEquals("<p>. {def}</p>", it) }
    }

    @Test
    fun `identity function chain`() {
        execute(".{10}::multiply {2}::sum {5}") {
            assertEquals("<p>25</p>", it)
        }
    }

    @Test
    fun `wrapped identity function`() {
        execute("hello{.{world}}hello") {
            assertEquals("<p>helloworldhello</p>", it)
        }
    }
}
