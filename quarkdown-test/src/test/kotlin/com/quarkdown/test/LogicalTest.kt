package com.quarkdown.test

import com.quarkdown.test.util.execute
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for boolean operators.
 */
class LogicalTest {
    private val yes = """<p><input disabled="" type="checkbox" checked="" /></p>"""
    private val no = """<p><input disabled="" type="checkbox" /></p>"""
    private val x = ".var {x} {5}\n\n"

    @Test
    fun `and is true only when both operands are true`() {
        execute(".and {yes} {yes}") { assertEquals(yes, it) }
        execute(".and {yes} {no}") { assertEquals(no, it) }
        execute(".and {no} {yes}") { assertEquals(no, it) }
        execute(".and {no} {no}") { assertEquals(no, it) }
    }

    @Test
    fun `or is true when at least one operand is true`() {
        execute(".or {yes} {yes}") { assertEquals(yes, it) }
        execute(".or {yes} {no}") { assertEquals(yes, it) }
        execute(".or {no} {yes}") { assertEquals(yes, it) }
        execute(".or {no} {no}") { assertEquals(no, it) }
    }

    @Test
    fun `and chained on a comparison`() {
        execute("$x.x::islower than:{10}::and {.x::isgreater than:{0}}") {
            assertEquals(yes, it)
        }
        execute("$x.x::islower than:{10}::and {.x::isgreater than:{7}}") {
            assertEquals(no, it)
        }
    }

    @Test
    fun `or chained on a comparison`() {
        execute("$x.x::islower than:{0}::or {.x::isgreater than:{3}}") {
            assertEquals(yes, it)
        }
        execute("$x.x::islower than:{0}::or {.x::isgreater than:{10}}") {
            assertEquals(no, it)
        }
    }

    @Test
    fun `operators combine with not`() {
        execute(".and {yes} {no}::not") { assertEquals(yes, it) }
        execute(".or {yes} {no}::not") { assertEquals(no, it) }
    }

    @Test
    fun `operators drive a conditional`() {
        execute(
            x +
                """
                .if {.x::islower than:{10}::and {.x::isgreater than:{0}}}
                    in range

                .if {.x::islower than:{0}::or {.x::isgreater than:{10}}}
                    unreachable
                """.trimIndent(),
        ) {
            assertEquals("<p>in range</p>", it)
        }
    }
}
