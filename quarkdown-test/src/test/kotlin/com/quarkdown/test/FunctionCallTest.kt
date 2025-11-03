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
}
