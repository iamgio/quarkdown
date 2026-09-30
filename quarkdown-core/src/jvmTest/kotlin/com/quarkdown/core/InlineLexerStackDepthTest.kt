package com.quarkdown.core

import com.quarkdown.core.flavor.quarkdown.QuarkdownFlavor
import com.quarkdown.core.lexer.Token
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tokenizes long inline runs on a thread with a small stack, so the emphasis resolver's pairing and nesting
 * stay bounded in stack frames. The resolver walks a linked list and never recurses, and these cases are what
 * hold it to that.
 */
class InlineLexerStackDepthTest {
    private val stackBytes = 256L * 1024

    private fun tokenizeOnSmallStack(source: String): List<Token> {
        var tokens: List<Token>? = null
        var failure: Throwable? = null
        val thread =
            Thread(null, {
                try {
                    tokens =
                        QuarkdownFlavor.lexerFactory
                            .newInlineLexer(source)
                            .tokenize()
                            .toList()
                } catch (e: Throwable) {
                    failure = e
                }
            }, "inline lexer", stackBytes)
        thread.start()
        thread.join()
        failure?.let { throw AssertionError("Tokenization overflowed a ${stackBytes / 1024} KB stack", it) }
        return tokens!!
    }

    /**
     * @param source source to tokenize
     * @return the tokens, asserted to cover the source exactly once
     */
    private fun assertCovers(source: String): List<Token> {
        val tokens = tokenizeOnSmallStack(source)
        assertEquals(source.length, tokens.sumOf { it.data.text.length }, "coverage")
        return tokens
    }

    @Test
    fun `long run of emphasis pairs`() {
        // Adjacent pairs share their delimiter runs, so how many tokens come out is the algorithm's business;
        // what matters here is that the run does not overflow and that every character is still covered.
        assertTrue(assertCovers("*a*".repeat(5000)).isNotEmpty())
    }

    @Test
    fun `long run of unpaired delimiters`() {
        assertTrue(assertCovers("*".repeat(5000)).isNotEmpty())
    }

    @Test
    fun `deeply nested emphasis`() {
        val depth = 2000
        assertTrue(assertCovers("*".repeat(depth) + "a" + "*".repeat(depth)).isNotEmpty())
    }

    @Test
    fun `long run of code spans and links`() {
        val tokens = assertCovers((1..5000).joinToString(" ") { "`c$it` [l$it](/u$it)" })
        assertTrue(tokens.size > 5000)
    }
}
