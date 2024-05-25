package eu.iamgio.quarkdown

import eu.iamgio.quarkdown.flavor.MarkdownFlavor
import eu.iamgio.quarkdown.flavor.base.BaseMarkdownFlavor
import eu.iamgio.quarkdown.flavor.quarkdown.QuarkdownFlavor
import eu.iamgio.quarkdown.lexer.Lexer
import eu.iamgio.quarkdown.lexer.Token
import eu.iamgio.quarkdown.lexer.TokenData
import eu.iamgio.quarkdown.lexer.regex.StandardRegexLexer
import eu.iamgio.quarkdown.lexer.regex.pattern.TokenRegexPattern
import eu.iamgio.quarkdown.lexer.tokens.BlockCodeToken
import eu.iamgio.quarkdown.lexer.tokens.BlockQuoteToken
import eu.iamgio.quarkdown.lexer.tokens.CodeSpanToken
import eu.iamgio.quarkdown.lexer.tokens.CommentToken
import eu.iamgio.quarkdown.lexer.tokens.DiamondAutolinkToken
import eu.iamgio.quarkdown.lexer.tokens.EmphasisToken
import eu.iamgio.quarkdown.lexer.tokens.EntityToken
import eu.iamgio.quarkdown.lexer.tokens.EscapeToken
import eu.iamgio.quarkdown.lexer.tokens.FencesCodeToken
import eu.iamgio.quarkdown.lexer.tokens.FunctionCallToken
import eu.iamgio.quarkdown.lexer.tokens.HeadingToken
import eu.iamgio.quarkdown.lexer.tokens.HorizontalRuleToken
import eu.iamgio.quarkdown.lexer.tokens.HtmlToken
import eu.iamgio.quarkdown.lexer.tokens.ImageToken
import eu.iamgio.quarkdown.lexer.tokens.InlineMathToken
import eu.iamgio.quarkdown.lexer.tokens.LineBreakToken
import eu.iamgio.quarkdown.lexer.tokens.LinkDefinitionToken
import eu.iamgio.quarkdown.lexer.tokens.LinkToken
import eu.iamgio.quarkdown.lexer.tokens.MultilineMathToken
import eu.iamgio.quarkdown.lexer.tokens.NewlineToken
import eu.iamgio.quarkdown.lexer.tokens.OnelineMathToken
import eu.iamgio.quarkdown.lexer.tokens.OrderedListToken
import eu.iamgio.quarkdown.lexer.tokens.PageBreakToken
import eu.iamgio.quarkdown.lexer.tokens.ParagraphToken
import eu.iamgio.quarkdown.lexer.tokens.PlainTextToken
import eu.iamgio.quarkdown.lexer.tokens.ReferenceImageToken
import eu.iamgio.quarkdown.lexer.tokens.ReferenceLinkToken
import eu.iamgio.quarkdown.lexer.tokens.SetextHeadingToken
import eu.iamgio.quarkdown.lexer.tokens.StrongEmphasisToken
import eu.iamgio.quarkdown.lexer.tokens.StrongToken
import eu.iamgio.quarkdown.lexer.tokens.TableToken
import eu.iamgio.quarkdown.lexer.tokens.UnorderedListToken
import eu.iamgio.quarkdown.lexer.tokens.UrlAutolinkToken
import eu.iamgio.quarkdown.lexer.walker.SourceReader
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
        QuarkdownFlavor.lexerFactory.newInlineLexer(source.trim())
            .tokenize().asSequence()
            .filter { it !is NewlineToken }
            .iterator()

    @Test
    fun sourceReader() {
        val reader = SourceReader("Test")
        assertEquals('T', reader.read())
        assertEquals('e', reader.peek())
        assertEquals('e', reader.read())
        assertEquals('s', reader.read())
        assertEquals('t', reader.read())
        assertNull(reader.read())
    }

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
            blockLexer(readSource("/lexing/blocks.md")).tokenize().asSequence()
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
        assertIs<HtmlToken>(tokens.next())
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
        val sources = readSource("/lexing/emphasis.md").split("${System.lineSeparator()}---${System.lineSeparator()}").iterator()

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
    fun flavors() {
        // Quarkdown features are not detected when using BaseMarkdownFlavor
        val tokens = blockLexer(readSource("/lexing/blocks.md"), flavor = BaseMarkdownFlavor).tokenize()
        assertTrue(tokens.filterIsInstance<MultilineMathToken>().isEmpty())
        assertTrue(tokens.filterIsInstance<OnelineMathToken>().isEmpty())
        assertFalse(tokens.filterIsInstance<BlockQuoteToken>().isEmpty())
    }
}
