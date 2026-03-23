package com.quarkdown.lsp

import com.quarkdown.lsp.pattern.QuarkdownPatterns
import com.quarkdown.lsp.tokenizer.FunctionCallToken
import com.quarkdown.lsp.tokenizer.FunctionCallTokenizer
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
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
    fun `function call with chaining`() {
        val text = ".function1::function2"
        val calls = tokenizer.getFunctionCalls(text)

        assertEquals(1, calls.size)

        val call = calls.first()
        val tokens = call.tokens.iterator()

        assertEquals(FunctionCallToken.Type.BEGIN, tokens.next().type)
        assertEquals(FunctionCallToken.Type.FUNCTION_NAME, tokens.next().type)
        assertEquals(FunctionCallToken.Type.CHAINING_SEPARATOR, tokens.next().type)
        assertEquals(FunctionCallToken.Type.FUNCTION_NAME, tokens.next().type)
        assertFalse(tokens.hasNext())
    }

    @Test
    fun `function call with chaining and args`() {
        val text = ".function1 {arg} name:{arg}::function2 {arg}"
        val calls = tokenizer.getFunctionCalls(text)

        assertEquals(1, calls.size)

        val call = calls.first()
        val tokens = call.tokens.iterator()

        assertEquals(FunctionCallToken.Type.BEGIN, tokens.next().type)
        assertEquals(FunctionCallToken.Type.FUNCTION_NAME, tokens.next().type)
        assertEquals(FunctionCallToken.Type.INLINE_ARGUMENT_BEGIN, tokens.next().type)
        assertEquals(FunctionCallToken.Type.INLINE_ARGUMENT_VALUE, tokens.next().type)
        assertEquals(FunctionCallToken.Type.INLINE_ARGUMENT_END, tokens.next().type)
        assertEquals(FunctionCallToken.Type.PARAMETER_NAME, tokens.next().type)
        assertEquals(FunctionCallToken.Type.NAMED_PARAMETER_DELIMITER, tokens.next().type)
        assertEquals(FunctionCallToken.Type.INLINE_ARGUMENT_BEGIN, tokens.next().type)
        assertEquals(FunctionCallToken.Type.INLINE_ARGUMENT_VALUE, tokens.next().type)
        assertEquals(FunctionCallToken.Type.INLINE_ARGUMENT_END, tokens.next().type)
        assertEquals(FunctionCallToken.Type.CHAINING_SEPARATOR, tokens.next().type)
        assertEquals(FunctionCallToken.Type.FUNCTION_NAME, tokens.next().type)
        assertEquals(FunctionCallToken.Type.INLINE_ARGUMENT_BEGIN, tokens.next().type)
        assertEquals(FunctionCallToken.Type.INLINE_ARGUMENT_VALUE, tokens.next().type)
        assertEquals(FunctionCallToken.Type.INLINE_ARGUMENT_END, tokens.next().type)
        assertFalse(tokens.hasNext())
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

    @Test
    fun `wrapped function call`() {
        val text = "{.function {x}}"
        val calls = tokenizer.getFunctionCalls(text)

        assertEquals(1, calls.size)

        val call = calls.first()
        assertEquals(0..text.length, call.range)

        val nameToken = call.tokens.find { it.type == FunctionCallToken.Type.FUNCTION_NAME }
        assertNotNull(nameToken)
        assertEquals("function", nameToken.lexeme)

        // The wrapping braces appear as argument delimiters in the grammar tokens.
        val argBeginTokens = call.tokens.filter { it.type == FunctionCallToken.Type.INLINE_ARGUMENT_BEGIN }
        val argEndTokens = call.tokens.filter { it.type == FunctionCallToken.Type.INLINE_ARGUMENT_END }

        // Two opens: wrap '{' + argument '{'. Two closes: argument '}' + wrap '}'.
        assertEquals(2, argBeginTokens.size)
        assertEquals(2, argEndTokens.size)
    }

    @Test
    fun `tight wrapped function call`() {
        val text = "hello{.func {x}}hello"
        val calls = tokenizer.getFunctionCalls(text)

        assertEquals(1, calls.size)

        val call = calls.first()
        val nameToken = call.tokens.find { it.type == FunctionCallToken.Type.FUNCTION_NAME }
        assertNotNull(nameToken)
        assertEquals("func", nameToken.lexeme)

        // The call range starts at '{' and ends after the closing '}'.
        assertEquals(5..16, call.range)
    }

    @Test
    fun `wrapped function call with chaining`() {
        val text = "{.func1 {x}::func2 {y}}"
        val calls = tokenizer.getFunctionCalls(text)

        assertEquals(1, calls.size)

        val call = calls.first()
        val functionNames =
            call.tokens
                .filter { it.type == FunctionCallToken.Type.FUNCTION_NAME }
                .map { it.lexeme }

        assertEquals(listOf("func1", "func2"), functionNames)

        // The wrapping braces are the first and last tokens.
        assertEquals(FunctionCallToken.Type.INLINE_ARGUMENT_BEGIN, call.tokens.first().type)
        assertEquals(FunctionCallToken.Type.INLINE_ARGUMENT_END, call.tokens.last().type)
    }

    @Test
    fun `wrapped function call with named parameter`() {
        val text = "{.function param:{value}}"
        val calls = tokenizer.getFunctionCalls(text)

        assertEquals(1, calls.size)

        val call = calls.first()

        val paramNameToken = call.tokens.find { it.type == FunctionCallToken.Type.PARAMETER_NAME }
        assertNotNull(paramNameToken)
        assertEquals("param", paramNameToken.lexeme)
    }

    @Test
    fun `nameless function call`() {
        val text = ".{hello}"
        val calls = tokenizer.getFunctionCalls(text)

        assertEquals(1, calls.size)

        val call = calls.first()
        assertEquals(0..text.length, call.range)

        // No FUNCTION_NAME token should be present.
        val nameToken = call.tokens.find { it.type == FunctionCallToken.Type.FUNCTION_NAME }
        assertEquals(null, nameToken)

        // The BEGIN token is still present.
        val beginToken = call.tokens.find { it.type == FunctionCallToken.Type.BEGIN }
        assertNotNull(beginToken)
        assertEquals(0..1, beginToken.range)

        // The argument is tokenized normally.
        val argValueToken = call.tokens.find { it.type == FunctionCallToken.Type.INLINE_ARGUMENT_VALUE }
        assertNotNull(argValueToken)
        assertEquals("hello", argValueToken.lexeme)
    }

    @Test
    fun `nameless function call with chaining`() {
        val text = ".{x}::bar {y}"
        val calls = tokenizer.getFunctionCalls(text)

        assertEquals(1, calls.size)

        val call = calls.first()
        val tokens = call.tokens.iterator()

        assertEquals(FunctionCallToken.Type.BEGIN, tokens.next().type)
        // No FUNCTION_NAME for the nameless part — argument follows directly.
        assertEquals(FunctionCallToken.Type.INLINE_ARGUMENT_BEGIN, tokens.next().type)
        assertEquals(FunctionCallToken.Type.INLINE_ARGUMENT_VALUE, tokens.next().type)
        assertEquals(FunctionCallToken.Type.INLINE_ARGUMENT_END, tokens.next().type)
        assertEquals(FunctionCallToken.Type.CHAINING_SEPARATOR, tokens.next().type)
        // The chained function has a name.
        with(tokens.next()) {
            assertEquals(FunctionCallToken.Type.FUNCTION_NAME, type)
            assertEquals("bar", lexeme)
        }
        assertEquals(FunctionCallToken.Type.INLINE_ARGUMENT_BEGIN, tokens.next().type)
        assertEquals(FunctionCallToken.Type.INLINE_ARGUMENT_VALUE, tokens.next().type)
        assertEquals(FunctionCallToken.Type.INLINE_ARGUMENT_END, tokens.next().type)
        assertFalse(tokens.hasNext())
    }

    @Test
    fun `nameless function call in text`() {
        val text = "hello .{world} goodbye"
        val calls = tokenizer.getFunctionCalls(text)

        assertEquals(1, calls.size)

        val call = calls.first()
        val nameToken = call.tokens.find { it.type == FunctionCallToken.Type.FUNCTION_NAME }
        assertEquals(null, nameToken)

        val argValueToken = call.tokens.find { it.type == FunctionCallToken.Type.INLINE_ARGUMENT_VALUE }
        assertNotNull(argValueToken)
        assertEquals("world", argValueToken.lexeme)
    }

    @Test
    fun `wrapped nameless function call`() {
        val text = "{.{x}}"
        val calls = tokenizer.getFunctionCalls(text)

        assertEquals(1, calls.size)

        val call = calls.first()
        assertEquals(0..text.length, call.range)

        val nameToken = call.tokens.find { it.type == FunctionCallToken.Type.FUNCTION_NAME }
        assertEquals(null, nameToken)
    }
}
