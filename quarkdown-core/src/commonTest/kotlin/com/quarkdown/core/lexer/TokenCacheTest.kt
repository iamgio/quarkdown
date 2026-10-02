package com.quarkdown.core.lexer

import com.quarkdown.core.lexer.tokens.PlainTextToken
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for [TokenCache].
 */
class TokenCacheTest {
    private val cache = TokenCache()
    private val lexed = mutableListOf<String>()

    /**
     * A lexer that produces one token per character, recording each lexed source.
     */
    private fun newLexer(source: String): Lexer {
        lexed += source
        return object : Lexer {
            override val source = source

            override fun tokenize() = source.indices.asSequence().map { PlainTextToken(TokenData(source[it].toString(), it..it)) }
        }
    }

    private fun texts(source: String) = cache.getOrLex(source, ::newLexer).map { it.data.text }.toList()

    @Test
    fun `lexes a source`() {
        assertEquals(listOf("a", "b"), texts("ab"))
    }

    @Test
    fun `lexes a repeated source once`() {
        repeat(3) { assertEquals(listOf("a", "b"), texts("ab")) }
        assertEquals(listOf("ab"), lexed)
    }

    @Test
    fun `lexes distinct sources separately`() {
        assertEquals(listOf("a"), texts("a"))
        assertEquals(listOf("b"), texts("b"))
        assertEquals(listOf("a", "b"), lexed)
    }
}
