package com.quarkdown.core

import com.quarkdown.core.flavor.quarkdown.QuarkdownFlavor
import com.quarkdown.core.lexer.Lexer
import com.quarkdown.core.lexer.Token
import com.quarkdown.core.lexer.tokens.BlockCodeToken
import com.quarkdown.core.lexer.tokens.BlockQuoteToken
import com.quarkdown.core.lexer.tokens.FootnoteDefinitionToken
import com.quarkdown.core.lexer.tokens.ListItemToken
import com.quarkdown.core.lexer.tokens.OrderedListToken
import com.quarkdown.core.lexer.tokens.ParagraphToken
import com.quarkdown.core.lexer.tokens.SetextHeadingToken
import com.quarkdown.core.lexer.tokens.TableToken
import com.quarkdown.core.lexer.tokens.UnorderedListToken
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

/**
 * Tests that tokenize long blocks on a thread with a small stack to make sure recursion stack depth stays bounded.
 */
class BlockLexerStackDepthTest {
    private val stackBytes = 256L * 1024

    private fun tokenizeOnSmallStack(
        source: String,
        lexer: (CharSequence) -> Lexer = QuarkdownFlavor.lexerFactory::newBlockLexer,
    ): List<Token> {
        var tokens: List<Token>? = null
        var failure: Throwable? = null
        val thread =
            Thread(null, {
                try {
                    tokens = lexer(source).tokenize().toList()
                } catch (e: Throwable) {
                    failure = e
                }
            }, "lexer", stackBytes)
        thread.start()
        thread.join()
        failure?.let { throw AssertionError("Tokenization overflowed a ${stackBytes / 1024} KB stack", it) }
        return tokens!!
    }

    @Test
    fun `long paragraph`() {
        val tokens = tokenizeOnSmallStack((1..5000).joinToString("\n") { "line $it" })
        assertEquals(1, tokens.size)
        assertIs<ParagraphToken>(tokens.single())
    }

    @Test
    fun `long paragraph followed by an underline`() {
        val tokens = tokenizeOnSmallStack((1..5000).joinToString("\n") { "line $it" } + "\n===")
        assertEquals(1, tokens.size)
        assertIs<SetextHeadingToken>(tokens.single())
    }

    @Test
    fun `long block quote`() {
        val tokens = tokenizeOnSmallStack((1..5000).joinToString("\n") { "> line $it" })
        assertEquals(1, tokens.size)
        assertIs<BlockQuoteToken>(tokens.single())
    }

    @Test
    fun `long table`() {
        val tokens = tokenizeOnSmallStack("|a|b|\n|-|-|\n" + (1..5000).joinToString("\n") { "|$it|x|" })
        assertEquals(1, tokens.size)
        assertIs<TableToken>(tokens.single())
    }

    @Test
    fun `long footnote definition`() {
        val tokens = tokenizeOnSmallStack("[^note]: first\n" + (1..5000).joinToString("\n") { "line $it" })
        assertEquals(1, tokens.size)
        assertIs<FootnoteDefinitionToken>(tokens.single())
    }

    @Test
    fun `long indented code block with blank lines`() {
        val tokens = tokenizeOnSmallStack((1..5000).joinToString("\n") { "    code $it" } + "\n\n    last\n")
        assertEquals(1, tokens.size)
        assertIs<BlockCodeToken>(tokens.single())
    }

    @Test
    fun `long unordered list`() {
        val tokens = tokenizeOnSmallStack((1..5000).joinToString("\n") { "- item $it" })
        assertEquals(1, tokens.size)
        assertIs<UnorderedListToken>(tokens.single())
    }

    @Test
    fun `long ordered list`() {
        val tokens = tokenizeOnSmallStack((1..5000).joinToString("\n") { "$it. item" })
        assertEquals(1, tokens.size)
        assertIs<OrderedListToken>(tokens.single())
    }

    @Test
    fun `list item spanning many lines`() {
        val item = "- first\n" + (1..5000).joinToString("\n") { "  line $it" }
        val tokens = tokenizeOnSmallStack(item, QuarkdownFlavor.lexerFactory::newListLexer)
        assertEquals(1, tokens.size)
        assertIs<ListItemToken>(tokens.single())
    }

    @Test
    fun `list item holding a long nested list`() {
        val item = "- first\n" + (1..5000).joinToString("\n") { "  - nested $it" }
        val tokens = tokenizeOnSmallStack(item, QuarkdownFlavor.lexerFactory::newListLexer)
        assertEquals(1, tokens.size)
        assertIs<ListItemToken>(tokens.single())
    }

    @Test
    fun `long list of short nested lists`() {
        val tokens = tokenizeOnSmallStack((1..2000).joinToString("\n") { "- item $it\n  - nested" })
        assertEquals(1, tokens.size)
        assertIs<UnorderedListToken>(tokens.single())
    }
}
