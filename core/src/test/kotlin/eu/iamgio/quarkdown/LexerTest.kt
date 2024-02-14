package eu.iamgio.quarkdown

import eu.iamgio.quarkdown.lexer.BlockLexer
import eu.iamgio.quarkdown.lexer.Lexer
import eu.iamgio.quarkdown.lexer.regex.StandardRegexLexer
import eu.iamgio.quarkdown.lexer.regex.pattern.TokenRegexPattern
import eu.iamgio.quarkdown.lexer.regex.pattern.WhitespaceTokenRegexPattern
import eu.iamgio.quarkdown.lexer.type.BlockTokenType
import eu.iamgio.quarkdown.lexer.type.TokenType
import eu.iamgio.quarkdown.lexer.type.WhitespaceTokenType
import eu.iamgio.quarkdown.lexer.walker.SourceReader
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
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
        val lexer =
            StandardRegexLexer(
                "ABC\nABB\nDEF\nGHI\nDE",
                listOf(
                    object : TokenRegexPattern {
                        override val name = "FIRST"
                        override val tokenType =
                            object : TokenType {
                                override val name: String = "FIRST"
                            }
                        override val regex: Regex = "AB.".toRegex()
                    },
                    object : TokenRegexPattern {
                        override val name = "SECOND"
                        override val tokenType =
                            object : TokenType {
                                override val name: String = "SECOND"
                            }
                        override val regex: Regex = "DE.?".toRegex()
                    },
                    object : TokenRegexPattern {
                        override val name = "NEWLINE"
                        override val tokenType =
                            object : TokenType {
                                override val name: String = "NEWLINE"
                            }
                        override val regex: Regex = "\\R".toRegex()
                    },
                ),
                fillTokenType =
                    object : TokenType {
                        override val name: String = "FILL"
                    },
            )

        val tokens = lexer.tokenize().iterator()
        with(tokens.next()) {
            assertEquals("ABC", text)
            assertEquals("FIRST", type.name)
        }

        assertEquals("NEWLINE", tokens.next().type.name)

        with(tokens.next()) {
            assertEquals("ABB", text)
            assertEquals("FIRST", type.name)
        }

        assertEquals("NEWLINE", tokens.next().type.name)

        with(tokens.next()) {
            assertEquals("DEF", text)
            assertEquals("SECOND", type.name)
        }

        assertEquals("NEWLINE", tokens.next().type.name)

        with(tokens.next()) {
            assertEquals("GHI", text)
            assertEquals("FILL", type.name)
        }

        assertEquals("NEWLINE", tokens.next().type.name)

        with(tokens.next()) {
            assertEquals("DE", text)
            assertEquals("SECOND", type.name)
        }
    }

    @Test
    fun whitespaces() {
        val lexer =
            StandardRegexLexer(
                readSource("/lexing/whitespace.md"),
                patterns = WhitespaceTokenRegexPattern.values().toList(),
                fillTokenType = WhitespaceTokenType.NON_WHITESPACE,
            )

        val tokens = lexer.tokenize().map { it.type }.iterator()

        assertEquals(WhitespaceTokenType.NON_WHITESPACE, tokens.next()) // "First paragraph."
        assertEquals(WhitespaceTokenType.EOL, tokens.next())
        assertEquals(WhitespaceTokenType.EOL, tokens.next()) // Empty line
        assertEquals(WhitespaceTokenType.LEADING_INDENT, tokens.next())
        assertEquals(WhitespaceTokenType.NON_WHITESPACE, tokens.next()) // "Indented second paragraph."
        assertEquals(WhitespaceTokenType.EOL, tokens.next())
        assertEquals(WhitespaceTokenType.EOL, tokens.next()) // Empty line
        assertEquals(WhitespaceTokenType.LEADING_INDENT, tokens.next())
        assertEquals(WhitespaceTokenType.LEADING_INDENT, tokens.next())
        assertEquals(WhitespaceTokenType.NON_WHITESPACE, tokens.next()) // "Doubly indented third paragraph."
        assertEquals(WhitespaceTokenType.EOL, tokens.next())
        assertEquals(WhitespaceTokenType.EOL, tokens.next()) // Empty line
        assertEquals(WhitespaceTokenType.NON_WHITESPACE, tokens.next()) // "This is a"
        assertEquals(WhitespaceTokenType.TRAILING_INDENT, tokens.next()) // Line break
        assertEquals(WhitespaceTokenType.EOL, tokens.next())
        assertEquals(WhitespaceTokenType.NON_WHITESPACE, tokens.next()) // "line break"
        assertEquals(WhitespaceTokenType.EOL, tokens.next())
        assertEquals(WhitespaceTokenType.EOL, tokens.next()) // Empty line
        assertEquals(WhitespaceTokenType.NON_WHITESPACE, tokens.next()) // "This is"
        assertEquals(WhitespaceTokenType.MIDDLE_WHITESPACE, tokens.next()) // Ignored
        assertEquals(WhitespaceTokenType.NON_WHITESPACE, tokens.next()) // "ignored whitespace"
        assertEquals(WhitespaceTokenType.EOL, tokens.next())
        assertEquals(WhitespaceTokenType.EOL, tokens.next())
        assertFalse(tokens.hasNext())
    }

    @Test
    fun blocks() {
        val tokens =
            BlockLexer(readSource("/lexing/blocks.md")).tokenize().asSequence()
                .map { it.type }
                .filter { it != BlockTokenType.NEWLINE }
                .iterator()

        assertEquals(BlockTokenType.HEADING, tokens.next())
        assertEquals(BlockTokenType.PARAGRAPH, tokens.next())
        assertEquals(BlockTokenType.HEADING, tokens.next())
        assertEquals(BlockTokenType.PARAGRAPH, tokens.next())
        assertEquals(BlockTokenType.SETEXT_HEADING, tokens.next())
        assertEquals(BlockTokenType.PARAGRAPH, tokens.next())
        assertEquals(BlockTokenType.LIST, tokens.next())
        assertEquals(BlockTokenType.PARAGRAPH, tokens.next())
        assertEquals(BlockTokenType.LIST, tokens.next())
        assertEquals(BlockTokenType.LIST, tokens.next())
        assertEquals(BlockTokenType.LIST, tokens.next())
        assertEquals(BlockTokenType.BLOCKQUOTE, tokens.next())
        assertEquals(BlockTokenType.BLOCKQUOTE, tokens.next())
        assertEquals(BlockTokenType.BLOCK_CODE, tokens.next())
        assertEquals(BlockTokenType.FENCES_CODE, tokens.next())
        assertEquals(BlockTokenType.HORIZONTAL_RULE, tokens.next())
        assertEquals(BlockTokenType.HTML, tokens.next())
        assertEquals(BlockTokenType.LINK_DEFINITION, tokens.next())
        assertEquals(BlockTokenType.HORIZONTAL_RULE, tokens.next())
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
