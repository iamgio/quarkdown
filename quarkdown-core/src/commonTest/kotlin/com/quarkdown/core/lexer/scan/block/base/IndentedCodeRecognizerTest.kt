package com.quarkdown.core.lexer.scan.block.base

import com.quarkdown.core.lexer.scan.LineCursor
import com.quarkdown.core.lexer.scan.Lines
import com.quarkdown.core.lexer.tokens.BlockCodeToken
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class IndentedCodeRecognizerTest {
    private fun code(source: String) = IndentedCodeRecognizer.open(LineCursor(Lines.of(source), 0))?.token as BlockCodeToken?

    @Test
    fun `keeps a blank line when more code follows`() {
        assertEquals("    a\n\n    b\n", code("    a\n\n    b\n")?.data?.text)
        assertEquals("a\n\nb\n", code("    a\n\n    b\n")?.content)
    }

    @Test
    fun `absorbs the trailing blank run when the code ends`() {
        assertEquals("    a\n\n", code("    a\n\nb\n")?.data?.text)
        assertEquals("    a\n  \n  \n", code("    a\n  \n  \nb\n")?.data?.text)
    }

    @Test
    fun `strips four columns of indentation, whether spaces or a tab`() {
        assertEquals("code\n", code("\tcode\n")?.content)
        assertEquals("code\nmore\n", code("\tcode\n\tmore\n")?.content)
        assertEquals(" code\n", code("  \t code\n")?.content)
        assertEquals(" a\n", code("     a\n")?.content)
        assertEquals("\tcode\n", code("\t\tcode\n")?.content)
    }

    @Test
    fun `treats a tab as four columns of indentation`() {
        assertEquals("\tcode\n", code("\tcode\n")?.data?.text)
        assertNull(code("   code\n"))
        assertNull(code("    \n"))
    }
}
