package com.quarkdown.test

import com.quarkdown.core.function.error.FunctionCallRuntimeException
import com.quarkdown.core.function.error.MismatchingArgumentTypeException
import com.quarkdown.test.util.execute
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Tests for the `Math` stdlib module:
 * arithmetic functions, constants such as `.pi`, trigonometric helpers,
 * and post-processing helpers like `.truncate` and `.round`.
 */
class MathFunctionsTest {
    @Test
    fun `basic sum`() {
        execute(".sum {1} {2}") { assertEquals("<p>3</p>", it) }
    }

    @Test
    fun `sum chained with multiply`() {
        execute(".sum {1} {2}::multiply by:{3}") { assertEquals("<p>9</p>", it) }
    }

    @Test
    fun `long chain of arithmetic operations`() {
        execute(".sum {1} {2}::subtract {1}::multiply by:{3}::divide by:{3}") {
            assertEquals("<p>2</p>", it)
        }
    }

    @Test
    fun `pi truncated to two decimals`() {
        execute(".pi::truncate {2}") { assertEquals("<p>3.14</p>", it) }
    }

    @Test
    fun trigonometry() {
        execute(".cos {0}") { assertEquals("<p>1</p>", it) }
        execute(".sin {0}") { assertEquals("<p>0</p>", it) }
        execute(".tan {0}") { assertEquals("<p>0</p>", it) }
        execute(".cos {.pi}") { assertEquals("<p>-1</p>", it) }
        execute(".pi::multiply {2}::cos") { assertEquals("<p>1</p>", it) }
    }

    @Test
    fun `circle surface using nested calls`() {
        execute(
            """
            .var {radius} {8}

            If we try to calculate the **surface** of a circle of **radius .radius**,
            we will find out it is **.multiply {.pow {.radius} to:{2}} by:{.pi}**
            """.trimIndent(),
        ) {
            assertEquals(
                "<p>If we try to calculate the <strong>surface</strong> of a circle of <strong>radius 8</strong>, " +
                    "we will find out it is <strong>201.06194</strong></p>",
                it,
            )
        }
    }

    @Test
    fun `circle surface using chained calls`() {
        execute(
            """
            .var {radius} {8}

            If we try to calculate the **surface** of a circle of **radius .radius**,
            we will find out it is **.pow {.radius} to:{2}::multiply by:{.pi}**
            """.trimIndent(),
        ) {
            assertEquals(
                "<p>If we try to calculate the <strong>surface</strong> of a circle of <strong>radius 8</strong>, " +
                    "we will find out it is <strong>201.06194</strong></p>",
                it,
            )
        }
    }

    @Test
    fun `round drops decimals`() {
        execute(".pow {8} to:{2}::multiply by:{.pi}::round") {
            assertEquals("<p>201</p>", it)
        }
    }

    @Test
    fun `truncate to a positive number of decimals`() {
        execute(".pow {8} to:{2}::multiply by:{.pi}::truncate decimals:{2}") {
            assertEquals("<p>201.06</p>", it)
        }
    }

    @Test
    fun `truncate with one decimal still trims to integer when value is short`() {
        execute(".pow {8} to:{2}::multiply by:{.pi}::truncate decimals:{1}") {
            assertEquals("<p>201</p>", it)
        }
    }

    @Test
    fun `truncate with zero decimals`() {
        execute(".pow {8} to:{2}::multiply by:{.pi}::truncate decimals:{0}") {
            assertEquals("<p>201</p>", it)
        }
    }

    @Test
    fun `negative number of decimals is rejected at runtime`() {
        assertFailsWith<FunctionCallRuntimeException> {
            execute(".pow {8} to:{2}::multiply by:{.pi}::truncate decimals:{-1}") {}
        }
    }

    @Test
    fun `non-integer number of decimals fails type check`() {
        assertFailsWith<MismatchingArgumentTypeException> {
            execute(".pow {8} to:{2}::multiply by:{.pi}::truncate decimals:{1.5}") {}
        }
    }
}
