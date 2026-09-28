package com.quarkdown.core.lexer.scan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ScannerTest {
    private fun scanner(source: String) = Scanner(source)

    @Test
    fun `peeks, marks and slices`() {
        val scanner = scanner("abcdef")
        assertEquals('a', scanner.peek())
        assertEquals('b', scanner.peek(1))
        assertNull(scanner.previous())
        val mark = scanner.mark()
        assertEquals(3, scanner.takeWhile { it < 'd' })
        assertEquals("abc", scanner.sliceFrom(mark))
        assertEquals('c', scanner.previous())
    }

    @Test
    fun `respects its limit`() {
        val scanner = Scanner("abcdef", limit = 3)
        assertEquals(3, scanner.takeWhile { true })
        assertTrue(scanner.isAtEnd)
        assertNull(scanner.peek())
        assertFalse(scanner.take("def"))
    }

    @Test
    fun `takes characters, text and runs`() {
        val scanner = scanner("aaaBBc")
        assertEquals(3, scanner.takeRun('a'))
        assertTrue(scanner.take("bb", ignoreCase = true))
        assertFalse(scanner.take('x'))
        assertTrue(scanner.takeIf { it == 'c' })
        assertTrue(scanner.isAtEnd)
    }

    @Test
    fun `rewinds a failed step and keeps a successful one`() {
        val scanner = scanner("abZ")
        assertFalse(scanner.step { take('a') && take('b') && take('c') })
        assertEquals(0, scanner.index)
        assertTrue(scanner.step { take('a') && take('b') })
        assertEquals(2, scanner.index)
    }

    @Test
    fun `repeats transactionally and never spins`() {
        val scanner = scanner("ababaX")
        assertEquals(2, scanner.repeatWhile { take('a') && take('b') })
        assertEquals(4, scanner.index)
        assertEquals(0, scanner.repeatWhile { true })
    }

    @Test
    fun `returns a value from an attempt or rewinds`() {
        val scanner = scanner("12ab")
        assertNull(scanner.attempt { takeWhile { it.isLetter() }.takeIf { it > 0 } })
        assertEquals(0, scanner.index)
        assertEquals(2, scanner.attempt { takeWhile { it.isDigit() }.takeIf { it > 0 } })
        assertEquals(2, scanner.index)
    }

    @Test
    fun `matches balanced delimiters and ignores escaped ones`() {
        assertEquals(5, Matchers.balancedDelimiters("{a{b}c}rest", 1, '{', '}'))
        assertEquals(1, Matchers.unescapedMatch("{", 0, '{'))
        assertEquals(0, Matchers.unescapedMatch("\\{", 1, '{'))
    }

    @Test
    fun `classifies punctuation and symbols`() {
        listOf('!', '"', '#', '-', '_', '(', ')', '—', '。', '+', '$', '^', '`', '|', '~')
            .forEach { assertTrue(CharCategories.isPunctuationOrSymbol(it), "expected punctuation or symbol: $it") }
        listOf('a', 'Z', '0', ' ', '\n', 'é')
            .forEach { assertFalse(CharCategories.isPunctuationOrSymbol(it), "unexpected punctuation or symbol: $it") }
    }
}
