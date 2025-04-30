package com.quarkdown.core

import com.quarkdown.core.flavor.MarkdownFlavor
import com.quarkdown.core.flavor.base.BaseMarkdownFlavor
import com.quarkdown.core.flavor.quarkdown.QuarkdownFlavor
import com.quarkdown.core.lexer.Lexer
import com.quarkdown.core.lexer.Token
import com.quarkdown.core.lexer.TokenData
import com.quarkdown.core.lexer.patterns.TextSymbolReplacement
import com.quarkdown.core.lexer.regex.StandardRegexLexer
import com.quarkdown.core.lexer.regex.pattern.TokenRegexPattern
import com.quarkdown.core.lexer.tokens.BlockCodeToken
import com.quarkdown.core.lexer.tokens.BlockQuoteToken
import com.quarkdown.core.lexer.tokens.CodeSpanToken
import com.quarkdown.core.lexer.tokens.CommentToken
import com.quarkdown.core.lexer.tokens.DiamondAutolinkToken
import com.quarkdown.core.lexer.tokens.EmphasisToken
import com.quarkdown.core.lexer.tokens.EntityToken
import com.quarkdown.core.lexer.tokens.EscapeToken
import com.quarkdown.core.lexer.tokens.FencesCodeToken
import com.quarkdown.core.lexer.tokens.FunctionCallToken
import com.quarkdown.core.lexer.tokens.HeadingToken
import com.quarkdown.core.lexer.tokens.HorizontalRuleToken
import com.quarkdown.core.lexer.tokens.ImageToken
import com.quarkdown.core.lexer.tokens.InlineMathToken
import com.quarkdown.core.lexer.tokens.LineBreakToken
import com.quarkdown.core.lexer.tokens.LinkDefinitionToken
import com.quarkdown.core.lexer.tokens.LinkToken
import com.quarkdown.core.lexer.tokens.MultilineMathToken
import com.quarkdown.core.lexer.tokens.NewlineToken
import com.quarkdown.core.lexer.tokens.OnelineMathToken
import com.quarkdown.core.lexer.tokens.OrderedListToken
import com.quarkdown.core.lexer.tokens.PageBreakToken
import com.quarkdown.core.lexer.tokens.ParagraphToken
import com.quarkdown.core.lexer.tokens.PlainTextToken
import com.quarkdown.core.lexer.tokens.ReferenceImageToken
import com.quarkdown.core.lexer.tokens.ReferenceLinkToken
import com.quarkdown.core.lexer.tokens.SetextHeadingToken
import com.quarkdown.core.lexer.tokens.StrongEmphasisToken
import com.quarkdown.core.lexer.tokens.StrongToken
import com.quarkdown.core.lexer.tokens.TableToken
import com.quarkdown.core.lexer.tokens.TextSymbolToken
import com.quarkdown.core.lexer.tokens.UnorderedListToken
import com.quarkdown.core.lexer.tokens.UrlAutolinkToken
import com.quarkdown.core.parser.walker.funcall.FunctionCallWalkerParser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertIsNot
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tokenization tests.
 * @see Lexer
 */
class LexerTest {
    private fun blockLexer(
        source: CharSequence,
        flavor: MarkdownFlavor = QuarkdownFlavor,
    ) = flavor.lexerFactory.newBlockLexer(source)

    private fun inlineLex(source: CharSequence) =
        QuarkdownFlavor.lexerFactory
            .newInlineLexer(source.trim())
            .tokenize()
            .asSequence()
            .filter { it !is NewlineToken }
            .iterator()

    @Test
    fun regex() {
        val wrap: (TokenData) -> Token = { ParagraphToken(it) }

        val lexer =
            StandardRegexLexer(
                "ABC\nABB\nDEF\nGHI\nDE",
                listOf(
                    TokenRegexPattern(
                        name = "FIRST",
                        wrap = wrap,
                        regex = "AB.".toRegex(),
                    ),
                    TokenRegexPattern(
                        name = "SECOND",
                        wrap = wrap,
                        regex = "DE.?".toRegex(),
                    ),
                    TokenRegexPattern(
                        name = "NEWLINE",
                        wrap = wrap,
                        regex = "\\R".toRegex(),
                    ),
                ),
                fillTokenType = wrap,
            )

        val tokens = lexer.tokenize().iterator()

        fun nextText() = tokens.next().data.text

        assertEquals("ABC", nextText())
        assertEquals("\n", nextText())
        assertEquals("ABB", nextText())
        assertEquals("\n", nextText())
        assertEquals("DEF", nextText())
        assertEquals("\n", nextText())
        assertEquals("GHI", nextText())
        assertEquals("\n", nextText())
        assertEquals("DE", nextText())
    }

    @Test
    fun blocks() {
        val tokens =
            blockLexer(readSource("/lexing/blocks.md"))
                .tokenize()
                .asSequence()
                .filter { it !is NewlineToken }
                .iterator()

        assertIs<HeadingToken>(tokens.next())
        assertIs<ParagraphToken>(tokens.next())
        assertIs<HeadingToken>(tokens.next())
        assertIs<ParagraphToken>(tokens.next())
        assertIs<SetextHeadingToken>(tokens.next())
        assertIs<ParagraphToken>(tokens.next())
        assertIs<UnorderedListToken>(tokens.next())
        assertIs<UnorderedListToken>(tokens.next())
        assertIs<OrderedListToken>(tokens.next())
        assertIs<BlockQuoteToken>(tokens.next())
        assertIs<BlockQuoteToken>(tokens.next())
        assertIs<BlockQuoteToken>(tokens.next())
        assertIs<BlockQuoteToken>(tokens.next())
        assertIs<BlockCodeToken>(tokens.next())
        assertIs<FencesCodeToken>(tokens.next())
        assertIs<MultilineMathToken>(tokens.next())
        assertIs<OnelineMathToken>(tokens.next())
        assertIs<PageBreakToken>(tokens.next())
        assertIs<HorizontalRuleToken>(tokens.next())
        assertIs<LinkDefinitionToken>(tokens.next())
        assertIs<HorizontalRuleToken>(tokens.next())
        assertIs<TableToken>(tokens.next())
        assertIs<FunctionCallToken>(tokens.next())
        assertIs<ParagraphToken>(tokens.next())
        assertIs<UnorderedListToken>(tokens.next())
        assertIs<FunctionCallToken>(tokens.next())
        assertIs<ParagraphToken>(tokens.next())
        assertIs<ParagraphToken>(tokens.next())
        assertIs<FunctionCallToken>(tokens.next())
        assertIs<ParagraphToken>(tokens.next())

        assertFalse(tokens.hasNext())
    }

    @Test
    fun emphasis() {
        val sources =
            readSource("/lexing/emphasis.md").split("${System.lineSeparator()}---${System.lineSeparator()}").iterator()

        repeat(2) {
            with(inlineLex(sources.next())) {
                assertIs<StrongToken>(next())
                assertFalse(hasNext())
            }
        }

        repeat(2) {
            with(inlineLex(sources.next())) {
                assertIs<PlainTextToken>(next())
                assertIs<StrongToken>(next())
                assertIs<PlainTextToken>(next())
                assertIs<StrongToken>(next())
                assertIs<PlainTextToken>(next())
                assertIs<EmphasisToken>(next())
                assertFalse(hasNext())
            }
        }

        with(inlineLex(sources.next())) {
            assertIs<PlainTextToken>(next())
            assertIs<StrongToken>(next())
            assertIs<PlainTextToken>(next())
            assertIs<StrongToken>(next())
            assertIs<PlainTextToken>(next())
            assertFalse(hasNext())
        }

        with(inlineLex(sources.next())) {
            assertIs<PlainTextToken>(next())
            assertIs<StrongToken>(next())
            assertIs<PlainTextToken>(next())
            assertFalse(hasNext())
        }

        with(inlineLex(sources.next())) {
            assertIs<StrongToken>(next())
            assertFalse(hasNext())
        }

        with(inlineLex(sources.next())) {
            assertIs<StrongEmphasisToken>(next())
            assertFalse(hasNext())
        }

        with(inlineLex(sources.next())) {
            assertIs<PlainTextToken>(next())
            assertIs<StrongEmphasisToken>(next())
            assertIs<PlainTextToken>(next())
            assertFalse(hasNext())
        }

        with(inlineLex(sources.next())) {
            assertIs<PlainTextToken>(next())
            assertFalse(hasNext())
        }

        with(inlineLex(sources.next())) {
            assertIs<EmphasisToken>(next())
            assertIs<PlainTextToken>(next())
            assertFalse(hasNext())
        }

        with(inlineLex(sources.next())) {
            assertIs<PlainTextToken>(next())
            assertIs<StrongToken>(next())
            assertIs<PlainTextToken>(next())
            assertFalse(hasNext())
        }
    }

    @Test
    fun comments() {
        val tokens = inlineLex(readSource("/lexing/comment.md"))
        assertIsNot<CommentToken>(tokens.next())
        assertIs<CommentToken>(tokens.next())
        assertIsNot<CommentToken>(tokens.next())
        assertIs<CommentToken>(tokens.next())
        assertIsNot<CommentToken>(tokens.next())
        assertIs<CommentToken>(tokens.next())
        assertIsNot<CommentToken>(tokens.next())
        assertIs<CommentToken>(tokens.next())
        assertIsNot<CommentToken>(tokens.next())
        assertIs<CommentToken>(tokens.next())
        assertIsNot<CommentToken>(tokens.next())
    }

    @Test
    fun escape() {
        val tokens = inlineLex(readSource("/lexing/escape.md"))
        assertIsNot<EscapeToken>(tokens.next()) // 'Text '
        assertIs<EscapeToken>(tokens.next()) // \#
        assertIsNot<EscapeToken>(tokens.next()) // ' text \m '
        assertIs<EscapeToken>(tokens.next()) // \!
        assertIsNot<EscapeToken>(tokens.next()) // ' '
        assertIs<EscapeToken>(tokens.next()) // \.
        assertIs<EscapeToken>(tokens.next()) // \,
        assertIsNot<EscapeToken>(tokens.next()) // ' text'
    }

    @Test
    fun entity() {
        val tokens = inlineLex(readSource("/lexing/entity.md"))
        assertIs<EntityToken>(tokens.next())
        assertIs<PlainTextToken>(tokens.next())
        assertIs<EntityToken>(tokens.next())
        assertIs<PlainTextToken>(tokens.next())
        assertIs<EntityToken>(tokens.next())
        assertIs<EntityToken>(tokens.next())
        assertIs<PlainTextToken>(tokens.next())
        assertIs<EntityToken>(tokens.next())
        assertIs<LineBreakToken>(tokens.next())
        assertIs<EntityToken>(tokens.next())
        assertIs<PlainTextToken>(tokens.next())
        assertIs<EntityToken>(tokens.next())
        assertIs<PlainTextToken>(tokens.next())
        assertIs<EntityToken>(tokens.next())
        assertIs<EntityToken>(tokens.next())
        assertIs<EntityToken>(tokens.next())
        assertIs<PlainTextToken>(tokens.next())
        assertIs<EntityToken>(tokens.next())
    }

    @Test
    fun inlineFunction() {
        val tokens = inlineLex(readSource("/lexing/inlinefunction.md"))
        assertIs<FunctionCallToken>(tokens.next())
        assertIs<PlainTextToken>(tokens.next())
        assertIs<FunctionCallToken>(tokens.next())
        assertIs<PlainTextToken>(tokens.next())
        assertIs<FunctionCallToken>(tokens.next())
        assertIs<PlainTextToken>(tokens.next())
        assertIs<FunctionCallToken>(tokens.next())
        assertIs<PlainTextToken>(tokens.next())
        assertIs<FunctionCallToken>(tokens.next())
        assertIs<PlainTextToken>(tokens.next())
        assertIs<FunctionCallToken>(tokens.next())
        assertIs<PlainTextToken>(tokens.next())
        assertIs<StrongToken>(tokens.next())
        assertIs<PlainTextToken>(tokens.next())
        assertIs<FunctionCallToken>(tokens.next())
        assertIs<PlainTextToken>(tokens.next())
        assertIs<FunctionCallToken>(tokens.next())
        assertIs<PlainTextToken>(tokens.next())

        assertFalse(tokens.hasNext())
    }

    @Test
    fun inline() {
        val tokens = inlineLex(readSource("/lexing/inline.md"))

        assertIs<PlainTextToken>(tokens.next())
        assertIs<EscapeToken>(tokens.next())
        assertIs<PlainTextToken>(tokens.next())
        assertIs<CodeSpanToken>(tokens.next())
        assertIs<PlainTextToken>(tokens.next())
        assertIs<CodeSpanToken>(tokens.next())
        assertIs<PlainTextToken>(tokens.next())
        assertIs<LinkToken>(tokens.next())
        assertIs<LineBreakToken>(tokens.next())
        assertIs<PlainTextToken>(tokens.next())
        assertIs<StrongToken>(tokens.next())
        assertIs<PlainTextToken>(tokens.next())
        assertIs<EmphasisToken>(tokens.next())
        assertIs<PlainTextToken>(tokens.next())
        assertIs<StrongEmphasisToken>(tokens.next())
        assertIs<PlainTextToken>(tokens.next())
        assertIs<DiamondAutolinkToken>(tokens.next())
        assertIs<UrlAutolinkToken>(tokens.next())
        assertIs<PlainTextToken>(tokens.next())
        assertIs<LinkToken>(tokens.next())
        assertIs<PlainTextToken>(tokens.next())
        assertIs<LinkToken>(tokens.next())
        assertIs<PlainTextToken>(tokens.next())
        assertIs<ReferenceLinkToken>(tokens.next())
        assertIs<PlainTextToken>(tokens.next())
        assertIs<ReferenceLinkToken>(tokens.next())
        assertIs<PlainTextToken>(tokens.next())
        assertIs<ReferenceLinkToken>(tokens.next())
        assertIs<PlainTextToken>(tokens.next())
        assertIs<ImageToken>(tokens.next())
        assertIs<PlainTextToken>(tokens.next())
        assertIs<ReferenceImageToken>(tokens.next())
        assertIs<PlainTextToken>(tokens.next())
        assertIs<CommentToken>(tokens.next())
        assertIs<PlainTextToken>(tokens.next())
        assertIs<StrongToken>(tokens.next())
        assertIs<PlainTextToken>(tokens.next())
        assertIs<ReferenceLinkToken>(tokens.next())
        assertIs<PlainTextToken>(tokens.next())
        assertIs<InlineMathToken>(tokens.next())
        assertIs<PlainTextToken>(tokens.next())
        assertIs<FunctionCallToken>(tokens.next())
        assertIs<PlainTextToken>(tokens.next())

        assertFalse(tokens.hasNext())
    }

    @Test
    fun textReplacement() {
        val tokens = inlineLex(readSource("/lexing/textreplacement.md"))

        fun assertSymbolEquals(symbol: TextSymbolReplacement) =
            with(tokens.next()) {
                assertIs<TextSymbolToken>(this)
                assertEquals(symbol, this.symbol)
            }

        assertIs<PlainTextToken>(tokens.next())
        assertSymbolEquals(TextSymbolReplacement.ELLIPSIS)
        assertIs<PlainTextToken>(tokens.next())
        assertSymbolEquals(TextSymbolReplacement.COPYRIGHT)
        assertIs<PlainTextToken>(tokens.next())
        assertSymbolEquals(TextSymbolReplacement.EM_DASH)
        assertIs<PlainTextToken>(tokens.next())
        assertSymbolEquals(TextSymbolReplacement.EM_DASH)
        assertIs<PlainTextToken>(tokens.next())
        assertSymbolEquals(TextSymbolReplacement.TYPOGRAPHIC_RIGHT_APOSTROPHE)
        assertIs<PlainTextToken>(tokens.next())
        assertSymbolEquals(TextSymbolReplacement.DOUBLE_RIGHT_ARROW)
        assertIs<PlainTextToken>(tokens.next())
        assertSymbolEquals(TextSymbolReplacement.NOT_EQUAL)
        assertIs<PlainTextToken>(tokens.next())
        assertSymbolEquals(TextSymbolReplacement.SINGLE_RIGHT_ARROW)
        assertIs<PlainTextToken>(tokens.next())
        assertSymbolEquals(TextSymbolReplacement.LESS_EQUAL)
        assertIs<PlainTextToken>(tokens.next())
        assertSymbolEquals(TextSymbolReplacement.GREATER_EQUAL)
        assertIs<PlainTextToken>(tokens.next())
        assertSymbolEquals(TextSymbolReplacement.SINGLE_LEFT_ARROW)
        assertIs<PlainTextToken>(tokens.next())
        assertSymbolEquals(TextSymbolReplacement.ELLIPSIS)
        assertIs<PlainTextToken>(tokens.next()) // Soft line break
        assertSymbolEquals(TextSymbolReplacement.TYPOGRAPHIC_LEFT_APOSTROPHE)
        assertIs<PlainTextToken>(tokens.next())
        assertSymbolEquals(TextSymbolReplacement.TYPOGRAPHIC_RIGHT_APOSTROPHE)
        assertIs<PlainTextToken>(tokens.next())
        assertSymbolEquals(TextSymbolReplacement.TYPOGRAPHIC_RIGHT_APOSTROPHE)
        assertIs<PlainTextToken>(tokens.next())
        assertSymbolEquals(TextSymbolReplacement.TYPOGRAPHIC_LEFT_APOSTROPHE)
        assertIs<PlainTextToken>(tokens.next())
        assertSymbolEquals(TextSymbolReplacement.TYPOGRAPHIC_RIGHT_APOSTROPHE)
        assertIs<PlainTextToken>(tokens.next())
        assertSymbolEquals(TextSymbolReplacement.TYPOGRAPHIC_LEFT_QUOTATION_MARK)
        assertIs<PlainTextToken>(tokens.next())
        assertSymbolEquals(TextSymbolReplacement.TYPOGRAPHIC_RIGHT_QUOTATION_MARK)
        assertIs<PlainTextToken>(tokens.next())
        assertSymbolEquals(TextSymbolReplacement.TRADEMARK)
        assertIs<PlainTextToken>(tokens.next())
        assertSymbolEquals(TextSymbolReplacement.TYPOGRAPHIC_LEFT_QUOTATION_MARK)
        assertIs<PlainTextToken>(tokens.next())
        assertSymbolEquals(TextSymbolReplacement.TYPOGRAPHIC_RIGHT_QUOTATION_MARK)
        assertIs<PlainTextToken>(tokens.next())
    }

    @Test
    fun flavors() {
        // Quarkdown features are not detected when using BaseMarkdownFlavor
        val tokens = blockLexer(readSource("/lexing/blocks.md"), flavor = BaseMarkdownFlavor).tokenize()
        assertTrue(tokens.filterIsInstance<MultilineMathToken>().isEmpty())
        assertTrue(tokens.filterIsInstance<OnelineMathToken>().isEmpty())
        assertFalse(tokens.filterIsInstance<BlockQuoteToken>().isEmpty())
    }

    @Test
    fun functionCall() {
        fun walk(source: CharSequence) = FunctionCallWalkerParser(source, allowsBody = true).parse()

        with(walk(".function")) {
            assertEquals("function", value.name)
            assertEquals(".function".length, endIndex)
        }

        with(walk(".function something")) {
            assertEquals("function", value.name)
            assertEquals(".function".length, endIndex)
        }

        with(walk(".function {x}")) {
            assertEquals("function", value.name)
            assertEquals(".function {x}".length, endIndex)
            with(value.arguments.single()) {
                assertEquals("x", value)
                assertNull(name)
            }
        }

        with(walk(".function {x} {y}")) {
            assertEquals("function", value.name)
            assertEquals("x", value.arguments[0].value)
            assertEquals("y", value.arguments[1].value)
        }

        with(walk(".function {x {a} b} {y {hello {world}}} {}")) {
            assertEquals("function", value.name)
            assertEquals("x {a} b", value.arguments[0].value)
            assertEquals("y {hello {world}}", value.arguments[1].value)
            assertEquals("", value.arguments[2].value)
        }

        with(walk(".function firstname:{y} lastname:{z}")) {
            assertEquals("function", value.name)
            with(value.arguments[0]) {
                assertEquals("firstname", name)
                assertEquals("y", value)
            }
            with(value.arguments[1]) {
                assertEquals("lastname", name)
                assertEquals("z", value)
            }
        }

        with(walk(".function {x} firstname:{y} lastname:{z}")) {
            assertEquals("function", value.name)
            assertEquals("x", value.arguments[0].value)
            with(value.arguments[1]) {
                assertEquals("firstname", name)
                assertEquals("y", value)
            }
            with(value.arguments[2]) {
                assertEquals("lastname", name)
                assertEquals("z", value)
            }
        }

        with(
            walk(
                """
                .function {
                    x
                } name:{
                    y
                }
                """.trimIndent(),
            ),
        ) {
            assertEquals("function", value.name)
            assertEquals("x", value.arguments[0].value)
            with(value.arguments[1]) {
                assertEquals("name", name)
                assertEquals("y", value)
            }
        }

        with(
            walk(
                """
                .function {x} {y}
                  Body
                """.trimIndent(),
            ),
        ) {
            assertEquals("function", value.name)
            assertEquals("x", value.arguments[0].value)
            assertEquals("y", value.arguments[1].value)
            assertEquals("Body", value.bodyArgument?.value)
        }

        with(
            walk(
                """
                .function {x} {y}
                    Body body
                    body body
                    body
                      body
                """.trimIndent(),
            ),
        ) {
            assertEquals("function", value.name)
            assertEquals("x", value.arguments[0].value)
            assertEquals("y", value.arguments[1].value)
            assertEquals("Body body\nbody body\nbody\n  body", value.bodyArgument?.value)
        }

        with(
            walk(
                """
                .function {x} {y}
                    Body body
                    body
                    
                    body
                      body
                """.trimIndent(),
            ),
        ) {
            assertEquals("function", value.name)
            assertEquals("x", value.arguments[0].value)
            assertEquals("y", value.arguments[1].value)
            assertEquals("Body body\nbody\n\nbody\n  body", value.bodyArgument?.value)
        }

        with(
            walk(
                """
                .foreach {1..3}
                    Hi .sum {.1} {2} hello
                """.trimIndent(),
            ),
        ) {
            assertEquals("foreach", value.name)
            assertEquals("1..3", value.arguments[0].value)
            assertEquals("Hi .sum {.1} {2} hello", value.bodyArgument?.value)
        }

        with(walk(".function\n\n\nx")) {
            assertEquals("function", value.name)
            assertEquals(0, value.arguments.size)
        }

        with(walk(".function\n\n  p\nx")) {
            assertEquals("function", value.name)
            assertEquals(0, value.arguments.size)
            assertEquals("p", value.bodyArgument?.value?.trim())
        }

        with(walk(".function\n\n  \np\nx")) {
            assertEquals("function", value.name)
            assertEquals(0, value.arguments.size)
            assertNull(value.bodyArgument)
        }

        with(walk(".function\n\nfunction")) {
            assertEquals("function", value.name)
            assertEquals(0, value.arguments.size)
            assertNull(value.bodyArgument)
        }

        with(walk(".function\n\nfunction {arg1} {arg2}")) {
            assertEquals("function", value.name)
            assertEquals(0, value.arguments.size)
            assertNull(value.bodyArgument)
        }

        with(walk(".foo::bar")) {
            assertEquals("foo", value.name)
            assertEquals(0, value.arguments.size)
            assertEquals("bar", value.next!!.name)
            assertEquals(0, value.next!!.arguments.size)
        }

        with(walk(".foo {a} {b}::bar {c}")) {
            assertEquals("foo", value.name)
            assertEquals(2, value.arguments.size)
            assertEquals("bar", value.next!!.name)
            assertEquals(1, value.next!!.arguments.size)
        }

        with(walk(".foo {a} {b}::bar {c}::baz {d}")) {
            assertEquals("foo", value.name)
            assertEquals(2, value.arguments.size)
            assertEquals("bar", value.next!!.name)
            assertEquals(1, value.next!!.arguments.size)
            assertEquals("baz", value.next!!.next!!.name)
            assertEquals(
                1,
                value.next!!
                    .next!!
                    .arguments.size,
            )
        }
    }
}
