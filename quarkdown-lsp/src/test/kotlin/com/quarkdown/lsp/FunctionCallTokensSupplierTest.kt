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

// Token type of the '{' and '}' brackets of a function call argument.
private val TYPE_INLINE_ARGUMENT_DELIMITER = TokenType.FUNCTION_CALL_INLINE_ARGUMENT_DELIMITER

/**
 * Tests for tokenization of function calls.
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
            assertNext(TYPE_INLINE_ARGUMENT_DELIMITER, 19..20)
            // The argument content "arg1" is now recognized as an enum value
            assertNext(TokenType.ENUM, 20..24)
            assertNext(TYPE_INLINE_ARGUMENT_DELIMITER, 24..25)
        }
    }

    @Test
    fun `function call with one named argument`() {
        tokenize("some text .funcall name:{arg1} some other text.") {
            assertNext(TYPE_BEGIN, 10..11)
            assertNext(TYPE_NAME, 11..18)
            assertNext(TYPE_NAMED_PARAMETER, 19..23)
            assertNext(TYPE_NAMED_PARAMETER_SEPARATOR, 23..24)
            assertNext(TYPE_INLINE_ARGUMENT_DELIMITER, 24..25)
            assertNext(TokenType.ENUM, 25..29)
            assertNext(TYPE_INLINE_ARGUMENT_DELIMITER, 29..30)
        }
    }

    @Test
    fun `function call with one positional and one named argument`() {
        tokenize("some text .funcall {arg1} name:{arg2} some other text.") {
            assertNext(TYPE_BEGIN, 10..11)
            assertNext(TYPE_NAME, 11..18)
            assertNext(TYPE_INLINE_ARGUMENT_DELIMITER, 19..20)
            assertNext(TokenType.ENUM, 20..24)
            assertNext(TYPE_INLINE_ARGUMENT_DELIMITER, 24..25)
            assertNext(TYPE_NAMED_PARAMETER, 26..30)
            assertNext(TYPE_NAMED_PARAMETER_SEPARATOR, 30..31)
            assertNext(TYPE_INLINE_ARGUMENT_DELIMITER, 31..32)
            assertNext(TokenType.ENUM, 32..36)
            assertNext(TYPE_INLINE_ARGUMENT_DELIMITER, 36..37)
        }
    }

    @Test
    fun `function call with multiple positional and named arguments`() {
        tokenize(".funcall {value1} {value2} firstnamed:{value3} secondnamed:{value with {nested}}") {
            assertNext(TYPE_BEGIN, 0..1)
            assertNext(TYPE_NAME, 1..8)
            // First positional argument.
            assertNext(TYPE_INLINE_ARGUMENT_DELIMITER, 9..10)
            assertNext(TokenType.ENUM, 10..16)
            assertNext(TYPE_INLINE_ARGUMENT_DELIMITER, 16..17)
            // Second positional argument.
            assertNext(TYPE_INLINE_ARGUMENT_DELIMITER, 18..19)
            assertNext(TokenType.ENUM, 19..25)
            assertNext(TYPE_INLINE_ARGUMENT_DELIMITER, 25..26)
            // First named argument.
            assertNext(TYPE_NAMED_PARAMETER, 27..37)
            assertNext(TYPE_NAMED_PARAMETER_SEPARATOR, 37..38)
            assertNext(TYPE_INLINE_ARGUMENT_DELIMITER, 38..39)
            assertNext(TokenType.ENUM, 39..45)
            assertNext(TYPE_INLINE_ARGUMENT_DELIMITER, 45..46)
            // Second named argument.
            assertNext(TYPE_NAMED_PARAMETER, 47..58)
            assertNext(TYPE_NAMED_PARAMETER_SEPARATOR, 58..59)
            assertNext(TYPE_INLINE_ARGUMENT_DELIMITER, 59..60)
            // The argument content "value with {nested}" is not recognized as any specific value type.
            assertNext(TYPE_INLINE_ARGUMENT_DELIMITER, 79..80)
            // Note: Nested braces within arguments are not tokenized separately.
        }
    }

    @Test
    fun `multiline function call`() {
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
            assertNext(TYPE_INLINE_ARGUMENT_DELIMITER, 5..6) // {
            assertNext(TYPE_INLINE_ARGUMENT_DELIMITER, 7..8) // }
            assertNext(TYPE_NAMED_PARAMETER, 9..10) // b
            assertNext(TYPE_NAMED_PARAMETER_SEPARATOR, 10..11) // :
            assertNext(TYPE_INLINE_ARGUMENT_DELIMITER, 11..12) // {
            assertNext(TYPE_INLINE_ARGUMENT_DELIMITER, 12..13) // }
            assertNext(TYPE_NAMED_PARAMETER, 14..15) // c
            assertNext(TYPE_NAMED_PARAMETER_SEPARATOR, 15..16) // :
            assertNext(TYPE_INLINE_ARGUMENT_DELIMITER, 16..17) // {
            assertNext(TYPE_INLINE_ARGUMENT_DELIMITER, 18..19) // }
            assertNext(TYPE_BEGIN, 21..22) // .
            assertNext(TYPE_NAME, 22..23) // y
        }
    }

    @Test
    fun `chained function calls`() {
        tokenize(".func1::func2") {
            assertNext(TYPE_BEGIN, 0..1)
            assertNext(TYPE_NAME, 1..6)
            // In chained function calls, the second function name is tokenized as a named parameter.
            assertNext(TYPE_NAMED_PARAMETER, 8..13)
        }
    }

    @Test
    fun `function call with nested braces in argument`() {
        tokenize(".func param:{{nested}}") {
            assertNext(TYPE_BEGIN, 0..1)
            assertNext(TYPE_NAME, 1..5)
            assertNext(TYPE_NAMED_PARAMETER, 6..11)
            assertNext(TYPE_NAMED_PARAMETER_SEPARATOR, 11..12)
            assertNext(TYPE_INLINE_ARGUMENT_DELIMITER, 12..13)
            assertNext(TYPE_INLINE_ARGUMENT_DELIMITER, 21..22)
            // Note: Nested braces within arguments are not tokenized separately.
        }
    }

    @Test
    fun `function call with escaped characters`() {
        tokenize(".func param:{content with \\{ escaped brace}") {
            assertNext(TYPE_BEGIN, 0..1)
            assertNext(TYPE_NAME, 1..5)
            assertNext(TYPE_NAMED_PARAMETER, 6..11)
            assertNext(TYPE_NAMED_PARAMETER_SEPARATOR, 11..12)
            assertNext(TYPE_INLINE_ARGUMENT_DELIMITER, 12..13)
            assertNext(TYPE_INLINE_ARGUMENT_DELIMITER, 42..43)
            // Note: The escaped characters in the argument are not tokenized separately.
        }
    }

    @Test
    fun `function call with body argument`() {
        tokenize(
            """
            .blockfunc
              This is a body argument
              that spans multiple lines
                with different indentation
            """.trimIndent().normalizeLineSeparators().toString(),
        ) {
            assertNext(TYPE_BEGIN, 0..1)
            assertNext(TYPE_NAME, 1..10)
            // Note: The body argument content itself is not tokenized as semantic tokens.
        }
    }

    @Test
    fun `function call with mixed inline and body arguments`() {
        tokenize(
            """
            .mixed param1:{inline} named:{value}
              This is a body argument
              following inline arguments
            """.trimIndent().normalizeLineSeparators().toString(),
        ) {
            assertNext(TYPE_BEGIN, 0..1)
            assertNext(TYPE_NAME, 1..6)
            // The tokenizer identifies "param1" as a named parameter.
            assertNext(TYPE_NAMED_PARAMETER, 7..13)
            assertNext(TYPE_NAMED_PARAMETER_SEPARATOR, 13..14)
            assertNext(TYPE_INLINE_ARGUMENT_DELIMITER, 14..15)
            assertNext(TokenType.ENUM, 15..21)
            assertNext(TYPE_INLINE_ARGUMENT_DELIMITER, 21..22)
            // The tokenizer identifies "named" as a named parameter.
            assertNext(TYPE_NAMED_PARAMETER, 23..28)
            assertNext(TYPE_NAMED_PARAMETER_SEPARATOR, 28..29)
            assertNext(TYPE_INLINE_ARGUMENT_DELIMITER, 29..30)
            assertNext(TokenType.ENUM, 30..35)
            assertNext(TYPE_INLINE_ARGUMENT_DELIMITER, 35..36)
            // Note: Body argument content is not tokenized as semantic tokens.
        }
    }

    // Value types

    @Test
    fun `boolean argument`() {
        tokenize(".func param:{yes}") {
            assertNext(TYPE_BEGIN, 0..1)
            assertNext(TYPE_NAME, 1..5)
            assertNext(TYPE_NAMED_PARAMETER, 6..11)
            assertNext(TYPE_NAMED_PARAMETER_SEPARATOR, 11..12)
            assertNext(TYPE_INLINE_ARGUMENT_DELIMITER, 12..13)
            assertNext(TokenType.BOOLEAN, 13..16)
            assertNext(TYPE_INLINE_ARGUMENT_DELIMITER, 16..17)
        }
    }

    @Test
    fun `number argument`() {
        tokenize(".func param:{42}") {
            assertNext(TYPE_BEGIN, 0..1)
            assertNext(TYPE_NAME, 1..5)
            assertNext(TYPE_NAMED_PARAMETER, 6..11)
            assertNext(TYPE_NAMED_PARAMETER_SEPARATOR, 11..12)
            assertNext(TYPE_INLINE_ARGUMENT_DELIMITER, 12..13)
            assertNext(TokenType.NUMBER, 13..15)
            assertNext(TYPE_INLINE_ARGUMENT_DELIMITER, 15..16)
        }
    }

    @Test
    fun `size argument`() {
        tokenize(".func param:{10px}") {
            assertNext(TYPE_BEGIN, 0..1)
            assertNext(TYPE_NAME, 1..5)
            assertNext(TYPE_NAMED_PARAMETER, 6..11)
            assertNext(TYPE_NAMED_PARAMETER_SEPARATOR, 11..12)
            assertNext(TYPE_INLINE_ARGUMENT_DELIMITER, 12..13)
            assertNext(TokenType.SIZE, 13..17)
            assertNext(TYPE_INLINE_ARGUMENT_DELIMITER, 17..18)
        }
    }

    @Test
    fun `sizes argument`() {
        tokenize(".func param:{10px 20px 10in 50%}") {
            assertNext(TYPE_BEGIN, 0..1)
            assertNext(TYPE_NAME, 1..5)
            assertNext(TYPE_NAMED_PARAMETER, 6..11)
            assertNext(TYPE_NAMED_PARAMETER_SEPARATOR, 11..12)
            assertNext(TYPE_INLINE_ARGUMENT_DELIMITER, 12..13)
            assertNext(TokenType.SIZE, 13..31)
            assertNext(TYPE_INLINE_ARGUMENT_DELIMITER, 31..32)
        }
    }

    @Test
    fun `range argument`() {
        tokenize(".func param:{1..5}") {
            assertNext(TYPE_BEGIN, 0..1)
            assertNext(TYPE_NAME, 1..5)
            assertNext(TYPE_NAMED_PARAMETER, 6..11)
            assertNext(TYPE_NAMED_PARAMETER_SEPARATOR, 11..12)
            assertNext(TYPE_INLINE_ARGUMENT_DELIMITER, 12..13)
            assertNext(TokenType.RANGE, 13..17)
            assertNext(TYPE_INLINE_ARGUMENT_DELIMITER, 17..18)
        }
    }

    @Test
    fun `enum argument`() {
        tokenize(".func param:{spacebetween}") {
            assertNext(TYPE_BEGIN, 0..1)
            assertNext(TYPE_NAME, 1..5)
            assertNext(TYPE_NAMED_PARAMETER, 6..11)
            assertNext(TYPE_NAMED_PARAMETER_SEPARATOR, 11..12)
            assertNext(TYPE_INLINE_ARGUMENT_DELIMITER, 12..13)
            assertNext(TokenType.ENUM, 13..25)
            assertNext(TYPE_INLINE_ARGUMENT_DELIMITER, 25..26)
        }
    }
}
