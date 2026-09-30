package com.quarkdown.core.lexer.scan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame

class CommentsTest {
    @Test
    fun `finds where a comment ends`() {
        assertEquals(10, commentEndAt("<!-- a -->", 0))
        assertEquals(5, commentEndAt("<!-->", 0))
        assertEquals(6, commentEndAt("<!--->", 0))
        assertEquals(12, commentEndAt("a <!-- b --> c", 2))
        assertNull(commentEndAt("<!-- a", 0))
        assertNull(commentEndAt("a <!-- b -->", 0))
    }

    @Test
    fun `strips every comment and keeps the rest`() {
        assertEquals("a  b", "a <!-- c --> b".withoutComments())
        assertEquals("ab", "a<!--c--><!--d-->b".withoutComments())
        assertEquals("ab", "<!--c-->a<!-->b<!--->".withoutComments())
        assertEquals("a\nb", "a\n<!-- c\nd -->b".withoutComments())
    }

    @Test
    fun `keeps text that holds no whole comment`() {
        val plain = "a <!- b -> c"
        assertSame(plain, plain.withoutComments())
        assertEquals("a <!-- b", "a <!-- b".withoutComments())
        assertEquals("<!-- a b", "<!-- a b".withoutComments())
    }
}
