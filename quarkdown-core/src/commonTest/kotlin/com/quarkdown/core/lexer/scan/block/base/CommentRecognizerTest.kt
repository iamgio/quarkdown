package com.quarkdown.core.lexer.scan.block.base

import com.quarkdown.core.lexer.scan.LineCursor
import com.quarkdown.core.lexer.scan.Lines
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CommentRecognizerTest {
    private fun comment(source: String) =
        CommentRecognizer
            .open(LineCursor(Lines.of(source), 0))
            ?.token
            ?.data
            ?.text

    @Test
    fun `covers the comment only`() {
        assertEquals("<!-- c -->", comment("<!-- c -->\n"))
        assertEquals("<!-- c -->", comment("<!-- c -->\npara\n"))
        assertEquals("<!-- a\nb -->", comment("<!-- a\nb -->\n"))
    }

    @Test
    fun `accepts the abrupt closings and declines the rest`() {
        assertEquals("<!-->", comment("<!-->\n"))
        assertEquals("<!--->", comment("<!--->\n"))
        assertNull(comment("<!-- unterminated\n"))
        assertNull(comment("x <!-- c -->\n"))
    }
}
