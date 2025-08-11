package com.quarkdown.lsp

import com.quarkdown.core.util.normalizeLineSeparators
import com.quarkdown.lsp.highlight.FunctionCallTokensSupplier
import com.quarkdown.lsp.highlight.SimpleTokenData
import com.quarkdown.lsp.highlight.TokenType
import org.eclipse.lsp4j.SemanticTokensParams
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

// Token type of the 'begin' token in a function call.
private val TYPE_BEGIN = TokenType.FUNCTION_CALL_IDENTIFIER

// Token type of the function call name.
private val TYPE_NAME = TokenType.FUNCTION_CALL_IDENTIFIER

// Token type of the named parameter in a function call.
private val TYPE_NAMED_PARAMETER = TokenType.FUNCTION_CALL_NAMED_PARAMETER

// Token type of the ':' separator between a named parameter and its value.
private val TYPE_NAMED_PARAMETER_SEPARATOR = TokenType.FUNCTION_CALL_NAMED_PARAMETER

/**
 *
 */
class FunctionCallTokensSupplierTest {
    private val supplier = FunctionCallTokensSupplier()
    private val params = SemanticTokensParams()

    private fun tokenize(
        text: String,
        block: Iterator<SimpleTokenData>.() -> Unit = {},
    ): Iterator<SimpleTokenData> =
        supplier
            .getTokens(params, text)
            .iterator()
            .also { tokens ->
                block(tokens)
                assertFalse(tokens.hasNext(), "Expected no more tokens after processing")
            }

    private fun Iterator<SimpleTokenData>.assertNext(
        type: TokenType,
        range: IntRange,
    ) {
        val token = next()
        assertEquals(type, token.type)
        assertEquals(range, token.range)
    }

    @Test
    fun `no calls`() {
        tokenize("This is a test without function calls.")
    }

    @Test
    fun `only function call, no args`() {
        tokenize(".funcall") {
            // 'Begin'
            assertNext(TYPE_BEGIN, 0..1)
            // Name
            assertNext(TYPE_NAME, 1..8)
        }
    }

    @Test
    fun `function call followed by a dot`() {
        tokenize(".funcall.") {
            assertNext(TYPE_BEGIN, 0..1)
            assertNext(TYPE_NAME, 1..8)
        }
    }

    @Test
    fun `function call in other content, no args`() {
        tokenize("some text .funcall some other text.") {
            assertNext(TYPE_BEGIN, 10..11)
            assertNext(TYPE_NAME, 11..18)
        }
    }

    @Test
    fun `two function calls, no args, no other content`() {
        tokenize(".funcall1 .funcall2") {
            assertNext(TYPE_BEGIN, 0..1)
            assertNext(TYPE_NAME, 1..9)
            assertNext(TYPE_BEGIN, 10..11)
            assertNext(TYPE_NAME, 11..19)
        }
    }

    @Test
    fun `two function calls, no args, with other content`() {
        tokenize("some text .funcall1 some other text .funcall2.") {
            assertNext(TYPE_BEGIN, 10..11)
            assertNext(TYPE_NAME, 11..19)
            assertNext(TYPE_BEGIN, 36..37)
            assertNext(TYPE_NAME, 37..45)
        }
    }

    @Test
    fun `function call with one positional argument`() {
        tokenize("some text .funcall {arg1} some other text.") {
            assertNext(TYPE_BEGIN, 10..11)
            assertNext(TYPE_NAME, 11..18)
        }
    }

    @Test
    fun `function call with one named argument`() {
        tokenize("some text .funcall name:{arg1} some other text.") {
            assertNext(TYPE_BEGIN, 10..11)
            assertNext(TYPE_NAME, 11..18)
            assertNext(TYPE_NAMED_PARAMETER, 19..23)
            assertNext(TYPE_NAMED_PARAMETER_SEPARATOR, 23..24)
        }
    }

    @Test
    fun `function call with one positional and one named argument`() {
        tokenize("some text .funcall {arg1} name:{arg2} some other text.") {
            assertNext(TYPE_BEGIN, 10..11)
            assertNext(TYPE_NAME, 11..18)
            assertNext(TYPE_NAMED_PARAMETER, 26..30)
            assertNext(TYPE_NAMED_PARAMETER_SEPARATOR, 30..31)
        }
    }

    @Test
    fun `multiline function call with named parameters`() {
        tokenize(
            """
            .x a:{
            } b:{} c:{
            }
            
            .y
            """.trimIndent().normalizeLineSeparators().toString(),
        ) {
            assertNext(TYPE_BEGIN, 0..1)
            assertNext(TYPE_NAME, 1..2)
            assertNext(TYPE_NAMED_PARAMETER, 3..4) // a
            assertNext(TYPE_NAMED_PARAMETER_SEPARATOR, 4..5) // :
            assertNext(TYPE_NAMED_PARAMETER, 9..10) // b
            assertNext(TYPE_NAMED_PARAMETER_SEPARATOR, 10..11) // :
            assertNext(TYPE_NAMED_PARAMETER, 14..15) // c
            assertNext(TYPE_NAMED_PARAMETER_SEPARATOR, 15..16) // :
            assertNext(TYPE_BEGIN, 21..22) // .
            assertNext(TYPE_NAME, 22..23) // y
        }
    }
}
