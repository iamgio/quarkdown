package eu.iamgio.quarkdown

import eu.iamgio.quarkdown.lexer.BlockLexer
import eu.iamgio.quarkdown.lexer.StandardRegexLexer
import eu.iamgio.quarkdown.lexer.pattern.WhitespaceTokenRegexPattern
import eu.iamgio.quarkdown.lexer.type.WhitespaceTokenType
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tokenization tests.
 * @see BlockLexer
 */
class LexerTest {
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
