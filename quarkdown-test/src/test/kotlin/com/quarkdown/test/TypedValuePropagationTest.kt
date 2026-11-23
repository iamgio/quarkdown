package com.quarkdown.test

import com.quarkdown.test.util.execute
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests that typed values produced by native functions keep their type
 * when passed through dynamic parameters, such as those of `.otherwise`, variables and custom functions.
 *
 * In particular, a string that looks like a function call (e.g. `.cargo` in a file path)
 * must be rendered as plain text rather than parsed and evaluated as Quarkdown source.
 */
class TypedValuePropagationTest {
    private val expected = "<p>/home/user/.cargo/bin</p>"

    // A string value whose content contains `.cargo`, read verbatim from a file.
    private val stringValue = ".read {literal-call.txt}"

    @Test
    fun `string value is rendered as text`() {
        execute(stringValue) {
            assertEquals(expected, it)
        }
    }

    @Test
    fun `string value through otherwise is rendered as text`() {
        execute("$stringValue::otherwise {fallback}") {
            assertEquals(expected, it)
        }
    }

    @Test
    fun `string value through takeif is rendered as text`() {
        execute("$stringValue::takeif {x: yes}") {
            assertEquals(expected, it)
        }
    }

    @Test
    fun `string value stored in a variable is rendered as text`() {
        execute(
            """
            .var {path} {$stringValue}

            .path
            """.trimIndent(),
        ) {
            assertEquals(expected, it)
        }
    }

    @Test
    fun `string value passed to a custom function is rendered as text`() {
        execute(
            """
            .function {identity}
                x:
                .x

            .identity {$stringValue}
            """.trimIndent(),
        ) {
            assertEquals(expected, it)
        }
    }
}
