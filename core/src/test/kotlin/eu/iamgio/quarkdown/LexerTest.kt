package eu.iamgio.quarkdown

import eu.iamgio.quarkdown.lexer.BlockCodeToken
import eu.iamgio.quarkdown.lexer.BlockLexer
import eu.iamgio.quarkdown.lexer.BlockQuoteToken
import eu.iamgio.quarkdown.lexer.FencesCodeToken
import eu.iamgio.quarkdown.lexer.HeadingToken
import eu.iamgio.quarkdown.lexer.HorizontalRuleToken
import eu.iamgio.quarkdown.lexer.HtmlToken
import eu.iamgio.quarkdown.lexer.Lexer
import eu.iamgio.quarkdown.lexer.LinkDefinitionToken
import eu.iamgio.quarkdown.lexer.ListItemToken
import eu.iamgio.quarkdown.lexer.NewlineToken
import eu.iamgio.quarkdown.lexer.ParagraphToken
import eu.iamgio.quarkdown.lexer.SetextHeadingToken
import eu.iamgio.quarkdown.lexer.Token
import eu.iamgio.quarkdown.lexer.TokenDecorator
import eu.iamgio.quarkdown.lexer.regex.StandardRegexLexer
import eu.iamgio.quarkdown.lexer.regex.pattern.TokenRegexPattern
import eu.iamgio.quarkdown.lexer.walker.SourceReader
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

/**
 * Tokenization tests.
 * @see Lexer
 */
class LexerTest {
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
        val wrap: (Token) -> TokenDecorator = { ParagraphToken(it) }

        val lexer =
            StandardRegexLexer(
                "ABC\nABB\nDEF\nGHI\nDE",
                listOf(
                    object : TokenRegexPattern {
                        override val name = "FIRST"
                        override val wrap = wrap
                        override val regex = "AB.".toRegex()
                    },
                    object : TokenRegexPattern {
                        override val name = "SECOND"
                        override val wrap = wrap
                        override val regex = "DE.?".toRegex()
                    },
                    object : TokenRegexPattern {
                        override val name = "NEWLINE"
                        override val wrap = wrap
                        override val regex = "\\R".toRegex()
                    },
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
            BlockLexer(readSource("/lexing/blocks.md")).tokenize().asSequence()
                .filter { it !is NewlineToken }
                .iterator()

        assertIs<HeadingToken>(tokens.next())
        assertIs<ParagraphToken>(tokens.next())
        assertIs<HeadingToken>(tokens.next())
        assertIs<ParagraphToken>(tokens.next())
        assertIs<SetextHeadingToken>(tokens.next())
        assertIs<ParagraphToken>(tokens.next())
        assertIs<ListItemToken>(tokens.next())
        assertIs<ParagraphToken>(tokens.next())
        assertIs<ListItemToken>(tokens.next())
        assertIs<ListItemToken>(tokens.next())
        assertIs<ListItemToken>(tokens.next())
        assertIs<BlockQuoteToken>(tokens.next())
        assertIs<BlockQuoteToken>(tokens.next())
        assertIs<BlockCodeToken>(tokens.next())
        assertIs<FencesCodeToken>(tokens.next())
        assertIs<HorizontalRuleToken>(tokens.next())
        assertIs<HtmlToken>(tokens.next())
        assertIs<LinkDefinitionToken>(tokens.next())
        assertIs<HorizontalRuleToken>(tokens.next())
    }

    /*@Test
    fun headings() {
        val tokens =
            Lexer(readSource("/lexing/heading.md")).tokenize().asSequence()
                .map { it.type }
                .filterNot { it.isWhitespace() }
                .iterator()

        assertEquals(TokenType.HEADING, tokens.next())
        assertEquals(TokenType.TEXT, tokens.next())
        assertEquals(TokenType.HEADING, tokens.next())
        assertEquals(TokenType.TEXT, tokens.next())
        assertEquals(TokenType.HEADING, tokens.next())
        assertEquals(TokenType.TEXT, tokens.next())
        assertEquals(TokenType.TEXT, tokens.next()) // Not a title
        assertEquals(TokenType.TEXT, tokens.next()) // "a"
        assertEquals(TokenType.TEXT, tokens.next()) // Not a title
        assertEquals(TokenType.HEADING, tokens.next()) // Empty title
        assertEquals(TokenType.TEXT, tokens.next()) // Not a title
        assertEquals(TokenType.TEXT, tokens.next()) // Not a title
        assertEquals(TokenType.HEADING, tokens.next())
        assertEquals(TokenType.TEXT, tokens.next())
        assertEquals(TokenType.HEADING_CLOSE, tokens.next())
    }*/
}
