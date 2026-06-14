package com.quarkdown.test

import com.quarkdown.core.function.error.InvalidArgumentCountException
import com.quarkdown.test.util.execute
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFailsWith

/**
 * Tests for user-defined functions: declaration, parameter handling,
 * recursive calls, mutation of external variables and caller-context propagation.
 */
class FunctionDefinitionTest {
    @Test
    fun `parameterless function`() {
        execute(
            """
            .function {hello}
                *Hello*!

            .hello
            """.trimIndent(),
        ) {
            assertEquals("<p><em>Hello</em>!</p>", it)
        }
    }

    @Test
    fun `function with a single parameter`() {
        execute(
            """
            .function {hello}
                target:
                **Hello** .target!

            .hello {world}
            """.trimIndent(),
        ) {
            assertEquals("<p><strong>Hello</strong> world!</p>", it)
        }
    }

    @Test
    fun `missing required argument fails`() {
        assertFailsWith<InvalidArgumentCountException> {
            execute(
                """
                .function {hello}
                    target:
                    `Hello` .target!

                .hello
                """.trimIndent(),
            ) {}
        }
    }

    @Test
    fun `function mutates external variable via var overwrite`() {
        execute(
            """
            .var {num} {0}

            .function {increase}
                .var {num} {.num::sum {1}}

            .increase
            .num
            """.trimIndent(),
        ) {
            assertEquals("<p>1</p>", it)
        }
    }

    @Test
    fun `function mutates external variable via subscript overwrite`() {
        // Reassigning a variable by calling it as a function: `.num {newvalue}`.
        execute(
            """
            .var {num} {0}

            .function {increase}
                .num {.num::sum {1}}

            .increase
            .num
            """.trimIndent(),
        ) {
            assertEquals("<p>1</p>", it)
        }
    }

    @Test
    fun `caller context propagates through two function levels`() {
        // `b` calls `a` with `.y` as the argument: the value of `.y` (defined in `b`'s scope)
        // must be resolvable from inside `a`'s body.
        execute(
            """
            .function {a}
                x:
                .x

            .function {b}
                y:
                .a
                    .y

            .b {hi}
            """.trimIndent(),
        ) {
            assertEquals("<p>hi</p>", it)
        }
    }

    @Test
    fun `caller context propagates through three function levels`() {
        execute(
            """
            .function {a}
                x:
                .c
                    .x

            .function {b}
                y:
                .a
                    .y

            .function {c}
                z:
                .z

            .b {hi}
            """.trimIndent(),
        ) {
            assertEquals("<p>hi</p>", it)
        }
    }

    @Test
    fun `function with optional parameter falls back to none`() {
        // The `name?` parameter is optional; when omitted, `.otherwise` provides a default.
        execute(
            """
            .function {greet}
                name?:
                Hello, .name::otherwise {stranger}!

            .greet

            .greet {world}
            """.trimIndent(),
        ) {
            assertEquals(
                "<p>Hello, stranger!</p><p>Hello, world!</p>",
                it,
            )
        }
    }

    @Test
    fun `function accepts named arguments in any order`() {
        execute(
            """
            .function {greet}
                from to:
                **Hello .to** from .from

            .greet to:{world} from:{John}
            """.trimIndent(),
        ) {
            assertEquals(
                "<p><strong>Hello world</strong> from John</p>",
                it,
            )
        }
    }

    @Test
    fun `function overwrite from same source`() {
        execute(
            """
            .function {greet}
                name:
                Hello, .name!

            .greet {Alice}

            .function {greet}
                name:
                Hi, .name!

            .greet {Bob}
            """.trimIndent(),
        ) {
            assertEquals("<p>Hello, Alice!</p><p>Hi, Bob!</p>", it)
        }
    }

    @Test
    fun `function overwrite from same source fails if overwriting is forbidden`() {
        assertFails {
            execute(
                """
                .function {greet}
                    name:
                    Hello, .name!

                .function {greet}
                    name:
                    Hi, .name!
                """.trimIndent(),
                forbidFunctionOverwriting = true,
            ) {}
        }
    }

    @Test
    fun `function overwrite from stdlib`() {
        execute(
            """
            .uppercase {Hello}
            
            .function {uppercase}
                text:
                .text::lowercase
            
            .uppercase {Hello}
            """.trimIndent(),
        ) {
            assertEquals("<p>HELLO</p><p>hello</p>", it)
        }
    }

    @Test
    fun `function overwrite from stdlib fails if overwriting is forbidden`() {
        assertFails {
            execute(
                """
                .function {uppercase}
                    text:
                    .text::lowercase
                """.trimIndent(),
                forbidFunctionOverwriting = true,
            ) {}
        }
    }

    @Test
    fun `recursive function with conditional base case`() {
        execute(
            """
            .function {countdown}
                n:
                .n
                .if {.n::isgreater than:{1}}
                    .countdown {.subtract {.n} {1}}

            .countdown {3}
            """.trimIndent(),
        ) {
            assertEquals("<p>3 2 1</p>", it)
        }
    }
}
