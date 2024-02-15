package eu.iamgio.quarkdown

import eu.iamgio.quarkdown.lexer.BlockLexer
import eu.iamgio.quarkdown.lexer.Lexer
import eu.iamgio.quarkdown.lexer.RawBlockCode
import eu.iamgio.quarkdown.lexer.RawBlockQuote
import eu.iamgio.quarkdown.lexer.RawFencesCode
import eu.iamgio.quarkdown.lexer.RawHeading
import eu.iamgio.quarkdown.lexer.RawHorizontalRule
import eu.iamgio.quarkdown.lexer.RawHtml
import eu.iamgio.quarkdown.lexer.RawLinkDefinition
import eu.iamgio.quarkdown.lexer.RawListItem
import eu.iamgio.quarkdown.lexer.RawNewline
import eu.iamgio.quarkdown.lexer.RawParagraph
import eu.iamgio.quarkdown.lexer.RawSetextHeading
import eu.iamgio.quarkdown.lexer.RawToken
import eu.iamgio.quarkdown.lexer.RawTokenWrapper
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
        val wrapper: (RawToken) -> RawTokenWrapper = { RawParagraph(it) }

        val lexer =
            StandardRegexLexer(
                "ABC\nABB\nDEF\nGHI\nDE",
                listOf(
                    object : TokenRegexPattern {
                        override val name = "FIRST"
                        override val tokenWrapper = wrapper
                        override val regex = "AB.".toRegex()
                    },
                    object : TokenRegexPattern {
                        override val name = "SECOND"
                        override val tokenWrapper = wrapper
                        override val regex = "DE.?".toRegex()
                    },
                    object : TokenRegexPattern {
                        override val name = "NEWLINE"
                        override val tokenWrapper = wrapper
                        override val regex = "\\R".toRegex()
                    },
                ),
                fillTokenType = wrapper,
            )

        val tokens = lexer.tokenize().iterator()

        fun nextText() = tokens.next().token.text

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
                .filter { it !is RawNewline }
                .iterator()

        assertIs<RawHeading>(tokens.next())
        assertIs<RawParagraph>(tokens.next())
        assertIs<RawHeading>(tokens.next())
        assertIs<RawParagraph>(tokens.next())
        assertIs<RawSetextHeading>(tokens.next())
        assertIs<RawParagraph>(tokens.next())
        assertIs<RawListItem>(tokens.next())
        assertIs<RawParagraph>(tokens.next())
        assertIs<RawListItem>(tokens.next())
        assertIs<RawListItem>(tokens.next())
        assertIs<RawListItem>(tokens.next())
        assertIs<RawBlockQuote>(tokens.next())
        assertIs<RawBlockQuote>(tokens.next())
        assertIs<RawBlockCode>(tokens.next())
        assertIs<RawFencesCode>(tokens.next())
        assertIs<RawHorizontalRule>(tokens.next())
        assertIs<RawHtml>(tokens.next())
        assertIs<RawLinkDefinition>(tokens.next())
        assertIs<RawHorizontalRule>(tokens.next())
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
