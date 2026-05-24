package com.quarkdown.test

import com.quarkdown.core.function.error.UnresolvedReferenceException
import com.quarkdown.test.util.execute
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Tests for `.if` and `.ifnot` conditional statements,
 * including how their condition arguments are interpreted (truthy strings, booleans, optionality).
 */
class ConditionalTest {
    @Test
    fun `if with true expression renders body`() {
        execute(
            """
            .if { .islower {2} than:{3} }
                **Text**
            """.trimIndent(),
        ) {
            assertEquals("<p><strong>Text</strong></p>", it)
        }
    }

    @Test
    fun `if with false expression suppresses body`() {
        execute(
            """
            .if { .islower {3} than:{2} }
                **Text**
            """.trimIndent(),
        ) {
            assertEquals("", it)
        }
    }

    @Test
    fun `ifnot inverts the condition`() {
        execute(
            """
            .ifnot { .islower {3} than:{2} }
                **Text**
            """.trimIndent(),
        ) {
            assertEquals("<p><strong>Text</strong></p>", it)
        }
    }

    @Test
    fun `if and ifnot inside a defined function infer boolean from argument`() {
        execute(
            """
            .function {x}
                arg:
                .if {.arg}
                    Hi
                .ifnot {.arg}
                    Hello

            .x {no}
            """.trimIndent(),
        ) {
            assertEquals("<p>Hello</p>", it)
        }
    }

    @Test
    fun `if and ifnot on a variable infer boolean from value`() {
        execute(
            """
            .var {x} {no}
            .if {.x}
                Hi
            .ifnot {.x}
                Hello
            """.trimIndent(),
        ) {
            assertEquals("<p>Hello</p>", it)
        }
    }

    @Test
    fun `if with literal yes renders body`() {
        execute(
            """
            .if {yes}
                ok
            """.trimIndent(),
        ) {
            assertEquals("<p>ok</p>", it)
        }
    }

    @Test
    fun `if with literal no suppresses body`() {
        execute(
            """
            .if {no}
                ok
            """.trimIndent(),
        ) {
            assertEquals("", it)
        }
    }

    @Test
    fun `nested if blocks combine like a logical AND`() {
        execute(
            """
            .if {yes}
                .if {yes}
                    both true
                .if {no}
                    unreachable
            """.trimIndent(),
        ) {
            assertEquals("<p>both true</p>", it)
        }
    }

    @Test
    fun `nested function definitions inside an if branch`() {
        execute(
            """
            .if {yes}
                .function {hello}
                    name:
                    Hello, *.name*!

                #### .hello {world}
                .hello {iamgio}
            """.trimIndent(),
        ) {
            assertEquals(
                "<h4>Hello, <em>world</em>!</h4><p>Hello, <em>iamgio</em>!</p>",
                it,
            )
        }
    }

    @Test
    fun `function defined inside an if branch does not leak to outer scope`() {
        assertFailsWith<UnresolvedReferenceException> {
            execute(
                """
                .if {yes}
                    .function {hello}
                        *Hello*!

                .hello
                """.trimIndent(),
            ) {}
        }
    }
}
