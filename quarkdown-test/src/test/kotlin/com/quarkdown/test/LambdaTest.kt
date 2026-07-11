package com.quarkdown.test

import com.quarkdown.test.util.execute
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for lambda expressions: implicit `.1` parameters, explicit named parameters,
 * and how lambdas combine with higher-order functions such as `.takeif` and `.otherwise`.
 */
class LambdaTest {
    @Test
    fun `newer implicit parameters take precedence`() {
        execute(
            """
            .let {hello}
                .1

                .let {world}
                    .1
            """.trimIndent(),
        ) {
            assertEquals("<p>hello</p><p>world</p>", it)
        }
    }

    @Test
    fun `inline lambda binds implicit parameter`() {
        execute(
            """
            .takeif {3} { .islower {.1} than:{5} }
            """.trimIndent(),
        ) {
            assertEquals("<p>3</p>", it)
        }
    }

    @Test
    fun `inline lambda binds explicit named parameter`() {
        execute(
            """
            .takeif {3} { x: .islower {.x} than:{5} }
            """.trimIndent(),
        ) {
            assertEquals("<p>3</p>", it)
        }
    }

    @Test
    fun `inline lambda drops value when predicate is false`() {
        execute(
            """
            .takeif {3} { x: .islower {.x} than:{2} }
            """.trimIndent(),
        ) {
            assertEquals(
                "<p><span class=\"codespan-content\"><code>None</code></span></p>",
                it,
            )
        }
    }

    @Test
    fun `inline lambda in chained call`() {
        execute(
            """
            .takeif {3} {x: .iseven {.x}}::otherwise {0}
            """.trimIndent(),
        ) {
            assertEquals("<p>0</p>", it)
        }
    }

    @Test
    fun `inline lambda as fallback argument`() {
        execute(
            """
            .otherwise {.takeif {3} {x: .iseven {.x}}} {0}
            """.trimIndent(),
        ) {
            assertEquals("<p>0</p>", it)
        }
    }

    @Test
    fun `lambda with multiple explicit parameters used in foreach`() {
        // The lambda accepts two named parameters (key + value) and references both.
        execute(
            """
            .var {d}
                .dictionary
                    - x: 10
                    - y: 20

            .foreach {.d}
                k v:
                .k = .v
            """.trimIndent(),
        ) {
            assertEquals("<p>x = 10</p><p>y = 20</p>", it)
        }
    }

    @Test
    fun `legacy @lambda prefix is still accepted for backward compatibility`() {
        execute(
            """
            .takeif {3} {@lambda x: .islower {.x} than:{5} }
            """.trimIndent(),
        ) {
            assertEquals("<p>3</p>", it)
        }
    }
}
