@file:Suppress("ktlint:standard:no-wildcard-imports")

package eu.iamgio.quarkdown

import eu.iamgio.quarkdown.flavor.MarkdownFlavor
import eu.iamgio.quarkdown.flavor.base.BaseMarkdownFlavor
import eu.iamgio.quarkdown.flavor.quarkdown.QuarkdownFlavor
import eu.iamgio.quarkdown.lexer.BlockCodeToken
import eu.iamgio.quarkdown.lexer.BlockQuoteToken
import eu.iamgio.quarkdown.lexer.CodeSpanToken
import eu.iamgio.quarkdown.lexer.CommentToken
import eu.iamgio.quarkdown.lexer.DiamondAutolinkToken
import eu.iamgio.quarkdown.lexer.EmphasisToken
import eu.iamgio.quarkdown.lexer.EntityToken
import eu.iamgio.quarkdown.lexer.EscapeToken
import eu.iamgio.quarkdown.lexer.FencesCodeToken
import eu.iamgio.quarkdown.lexer.HeadingToken
import eu.iamgio.quarkdown.lexer.HorizontalRuleToken
import eu.iamgio.quarkdown.lexer.HtmlToken
import eu.iamgio.quarkdown.lexer.ImageToken
import eu.iamgio.quarkdown.lexer.Lexer
import eu.iamgio.quarkdown.lexer.LineBreakToken
import eu.iamgio.quarkdown.lexer.LinkDefinitionToken
import eu.iamgio.quarkdown.lexer.LinkToken
import eu.iamgio.quarkdown.lexer.MultilineMathToken
import eu.iamgio.quarkdown.lexer.NewlineToken
import eu.iamgio.quarkdown.lexer.OnelineMathToken
import eu.iamgio.quarkdown.lexer.OrderedListToken
import eu.iamgio.quarkdown.lexer.ParagraphToken
import eu.iamgio.quarkdown.lexer.PlainTextToken
import eu.iamgio.quarkdown.lexer.ReferenceImageToken
import eu.iamgio.quarkdown.lexer.ReferenceLinkToken
import eu.iamgio.quarkdown.lexer.SetextHeadingToken
import eu.iamgio.quarkdown.lexer.StrongEmphasisToken
import eu.iamgio.quarkdown.lexer.StrongToken
import eu.iamgio.quarkdown.lexer.TableToken
import eu.iamgio.quarkdown.lexer.Token
import eu.iamgio.quarkdown.lexer.TokenData
import eu.iamgio.quarkdown.lexer.UnorderedListToken
import eu.iamgio.quarkdown.lexer.UrlAutolinkToken
import eu.iamgio.quarkdown.lexer.regex.StandardRegexLexer
import eu.iamgio.quarkdown.lexer.regex.pattern.TokenRegexPattern
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
        assertIs<HorizontalRuleToken>(tokens.next())
        assertIs<HtmlToken>(tokens.next())
        assertIs<LinkDefinitionToken>(tokens.next())
        assertIs<HorizontalRuleToken>(tokens.next())
        assertIs<TableToken>(tokens.next())
    }

    @Test
    fun emphasis() {
        val sources = readSource("/lexing/emphasis.md").split("\n---\n").iterator()

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
