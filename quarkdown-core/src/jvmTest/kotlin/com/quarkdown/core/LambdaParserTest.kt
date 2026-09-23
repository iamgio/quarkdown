package com.quarkdown.core

import com.quarkdown.core.function.value.data.LambdaParameter
import com.quarkdown.core.parser.walker.lambda.LambdaParser
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for [LambdaParser].
 */
class LambdaParserTest {
    @Test
    fun `no parameters`() {
        val result = LambdaParser.parse("Hello world")
        assertEquals(emptyList(), result.parameters)
        assertEquals("Hello world", result.body)
    }

    @Test
    fun `single parameter`() {
        val result = LambdaParser.parse("n: .pow {.n} to:{2}")
        assertEquals(listOf(LambdaParameter("n")), result.parameters)
        assertEquals(".pow {.n} to:{2}", result.body)
    }

    @Test
    fun `multiple parameters`() {
        val result = LambdaParser.parse("a b: Hello .a and .b")
        assertEquals(
            listOf(LambdaParameter("a"), LambdaParameter("b")),
            result.parameters,
        )
        assertEquals("Hello .a and .b", result.body)
    }

    @Test
    fun `optional parameter`() {
        val result = LambdaParser.parse("x y?: body")
        assertEquals(
            listOf(LambdaParameter("x"), LambdaParameter("y", isOptional = true)),
            result.parameters,
        )
        assertEquals("body", result.body)
    }

    @Test
    fun `multiline body`() {
        val result = LambdaParser.parse("n:\n.pow {.n} to:{2}")
        assertEquals(listOf(LambdaParameter("n")), result.parameters)
        assertEquals(".pow {.n} to:{2}", result.body)
    }

    @Test
    fun `empty body`() {
        val result = LambdaParser.parse("n:")
        assertEquals(listOf(LambdaParameter("n")), result.parameters)
        assertEquals("", result.body)
    }

    @Test
    fun `no colon means no parameters`() {
        val result = LambdaParser.parse(".sum {1} {2}")
        assertEquals(emptyList(), result.parameters)
        assertEquals(".sum {1} {2}", result.body)
    }

    @Test
    fun `escaped delimiter means no parameters`() {
        val result = LambdaParser.parse("n\\: not a lambda")
        assertEquals(emptyList(), result.parameters)
        assertEquals("n\\: not a lambda", result.body)
    }
}
