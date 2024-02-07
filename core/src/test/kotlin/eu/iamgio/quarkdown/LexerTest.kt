package eu.iamgio.quarkdown

import eu.iamgio.quarkdown.lexer.Lexer
import eu.iamgio.quarkdown.lexer.TokenType
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tokenization tests.
 * @see Lexer
 */
class LexerTest {
    @Test
    fun whitespaces() {
        val tokens = Lexer(readSource("/lexing/whitespace.md")).tokenize().map { it.type }.iterator()

        assertEquals(TokenType.TEXT, tokens.next()) // "First paragraph."
        assertEquals(TokenType.EOL, tokens.next())
        assertEquals(TokenType.EOL, tokens.next()) // Empty line
        assertEquals(TokenType.LEADING_INDENT, tokens.next())
        assertEquals(TokenType.TEXT, tokens.next()) // "Indented second paragraph."
        assertEquals(TokenType.EOL, tokens.next())
        assertEquals(TokenType.EOL, tokens.next()) // Empty line
        assertEquals(TokenType.LEADING_INDENT, tokens.next())
        assertEquals(TokenType.LEADING_INDENT, tokens.next())
        assertEquals(TokenType.TEXT, tokens.next()) // "Doubly indented third paragraph."
        assertEquals(TokenType.EOL, tokens.next())
        assertEquals(TokenType.EOL, tokens.next()) // Empty line
        assertEquals(TokenType.TEXT, tokens.next()) // "This is a"
        assertEquals(TokenType.TRAILING_INDENT, tokens.next()) // Line break
        assertEquals(TokenType.EOL, tokens.next())
        assertEquals(TokenType.TEXT, tokens.next()) // "line break"
        assertEquals(TokenType.EOL, tokens.next())
        assertEquals(TokenType.EOL, tokens.next()) // Empty line
        assertEquals(TokenType.TEXT, tokens.next()) // "This is"
        assertEquals(TokenType.MIDDLE_WHITESPACE, tokens.next()) // Ignored
        assertEquals(TokenType.TEXT, tokens.next()) // "ignored whitespace"
        assertEquals(TokenType.EOL, tokens.next())
        assertEquals(TokenType.EOL, tokens.next())
    }

    @Test
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
    }
}
