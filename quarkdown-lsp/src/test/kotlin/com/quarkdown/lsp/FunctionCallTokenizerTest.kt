package com.quarkdown.lsp

import com.quarkdown.lsp.pattern.QuarkdownPatterns
import com.quarkdown.lsp.tokenizer.FunctionCallToken
import com.quarkdown.lsp.tokenizer.FunctionCallTokenizer
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Tests for [com.quarkdown.lsp.tokenizer.FunctionCallTokenizer].
 *
 * These tests verify that the tokenizer correctly identifies function calls
 * and their components in various text patterns.
 */
class FunctionCallTokenizerTest {
    private val tokenizer = FunctionCallTokenizer()

    @Test
    fun `simple function call tokenization`() {
        val text = ".function"
        val calls = tokenizer.getFunctionCalls(text)

        assertEquals(1, calls.size)

        val call = calls.first()
        assertEquals(0..9, call.range)

        // Verify tokens
        val tokens = call.tokens
        assertEquals(2, tokens.size)

        val beginToken = tokens.find { it.type == FunctionCallToken.Type.BEGIN }
        assertNotNull(beginToken)
        assertEquals(QuarkdownPatterns.FunctionCall.BEGIN, beginToken.lexeme)
        assertEquals(0..1, beginToken.range)

        val nameToken = tokens.find { it.type == FunctionCallToken.Type.FUNCTION_NAME }
        assertNotNull(nameToken)
        assertEquals("function", nameToken.lexeme)
        assertEquals(1..9, nameToken.range)
    }

    @Test
    fun `function call with named parameter`() {
        val text = ".function param:{value}"
        val calls = tokenizer.getFunctionCalls(text)

        assertEquals(1, calls.size)

        val call = calls.first()
        val tokens = call.tokens

        // Verify parameter tokens
        val paramNameToken = tokens.find { it.type == FunctionCallToken.Type.PARAMETER_NAME }
        assertNotNull(paramNameToken)
        assertEquals("param", paramNameToken.lexeme)

        val delimiterToken = tokens.find { it.type == FunctionCallToken.Type.NAMED_PARAMETER_DELIMITER }
        assertNotNull(delimiterToken)
        assertEquals(QuarkdownPatterns.FunctionCall.NAMED_ARGUMENT_DELIMITER, delimiterToken.lexeme)
    }

    @Test
    fun `function call with inline argument`() {
        val text = ".function {argument content}"
        val calls = tokenizer.getFunctionCalls(text)

        assertEquals(1, calls.size)

        val call = calls.first()
        val tokens = call.tokens

        // Verify inline argument tokens
        val beginToken = tokens.find { it.type == FunctionCallToken.Type.INLINE_ARGUMENT_BEGIN }
        assertNotNull(beginToken)
        assertEquals(QuarkdownPatterns.FunctionCall.ARGUMENT_BEGIN, beginToken.lexeme)

        val valueToken = tokens.find { it.type == FunctionCallToken.Type.INLINE_ARGUMENT_VALUE }
        assertNotNull(valueToken)
        assertEquals("argument content", valueToken.lexeme)

        val endToken = tokens.find { it.type == FunctionCallToken.Type.INLINE_ARGUMENT_END }
        assertNotNull(endToken)
        assertEquals(QuarkdownPatterns.FunctionCall.ARGUMENT_END, endToken.lexeme)
    }

    @Test
    fun `nested function calls`() {
        val text = ".outer {.inner {nested content}}"
        val calls = tokenizer.getFunctionCalls(text)

        // Should find both outer and inner function calls
        assertTrue(calls.size >= 2)

        // Verify outer call
        val outerCall =
            calls.find {
                it.tokens.any { token ->
                    token.type == FunctionCallToken.Type.FUNCTION_NAME && token.lexeme == "outer"
                }
            }
        assertNotNull(outerCall)
        assertEquals(0..text.length, outerCall.range)

        // Verify inner call
        val innerCall =
            calls.find {
                it.tokens.any { token ->
                    token.type == FunctionCallToken.Type.FUNCTION_NAME && token.lexeme == "inner"
                }
            }
        assertNotNull(innerCall)
        assertEquals(8..31, innerCall.range)
    }

    @Test
    fun `multiple function calls in text`() {
        val text = "Text .function1 {arg1} more text .function2 param:{value}"
        val calls = tokenizer.getFunctionCalls(text)

        assertEquals(2, calls.size)

        // Verify function names
        val functionNames =
            calls.flatMap { call ->
                call.tokens.filter { it.type == FunctionCallToken.Type.FUNCTION_NAME }.map { it.lexeme }
            }
        assertTrue("function1" in functionNames)
        assertTrue("function2" in functionNames)
    }
}
