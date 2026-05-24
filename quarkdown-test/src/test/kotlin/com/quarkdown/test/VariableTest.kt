package com.quarkdown.test

import com.quarkdown.test.util.execute
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for variable-related statements: `.var` declarations and reassignments,
 * `.let` scoped bindings, and how variables interact with enclosing scopes.
 */
class VariableTest {
    @Test
    fun `var declaration, reassignment via var and via subscript`() {
        execute(
            """
            .var {a} {0}

            .a

            .var {a} {1}

            .a

            .a {2}

            .a
            """.trimIndent(),
        ) {
            assertEquals("<p>0</p><p>1</p><p>2</p>", it)
        }
    }

    @Test
    fun `let binds a single value to a lambda parameter`() {
        execute(
            """
            .let {world}
                Hello, **.1**!
            """.trimIndent(),
        ) {
            assertEquals("<p>Hello, <strong>world</strong>!</p>", it)
        }
    }

    @Test
    fun `let with explicit parameter name inside foreach`() {
        execute(
            """
            .foreach {..3}
                .let {.1}
                    x:
                    .sum {3} {.x}
            """.trimIndent(),
        ) {
            assertEquals("<p>4</p><p>5</p><p>6</p>", it)
        }
    }

    @Test
    fun `let chained with read for file contents`() {
        execute(
            """
            .let {code.txt}
                file:
                .let {.read {.file} {..2}}
                    .code
                        .1
            """.trimIndent(),
        ) {
            assertEquals("<pre><code>Line 1\nLine 2</code></pre>", it)
        }
    }

    @Test
    fun `var declared inside let does not capture let parameter when assigned a literal`() {
        // The var holds the literal `A`, not the let parameter's value.
        execute(
            """
            .let {X}
                x:

                .var {a}
                    A

                .a
            """.trimIndent(),
        ) {
            assertEquals("<p>A</p>", it)
        }
    }

    @Test
    fun `var declared inside let can read the let parameter`() {
        execute(
            """
            .let {X}
                x:

                .var {a}
                    .x

                .a
            """.trimIndent(),
        ) {
            assertEquals("<p>X</p>", it)
        }
    }

    @Test
    fun `let nested inside another let exposes both bindings`() {
        execute(
            """
            .let {hello}
                greeting:
                .let {world}
                    name:
                    .greeting, .name!
            """.trimIndent(),
        ) {
            assertEquals("<p>hello, world!</p>", it)
        }
    }

    @Test
    fun `var reassignment from inside a foreach updates the outer variable`() {
        execute(
            """
            .var {total} {0}

            .foreach {..3}
                .var {total} {.total::sum {.1}}

            .total
            """.trimIndent(),
        ) {
            assertEquals("<p>6</p>", it)
        }
    }
}
