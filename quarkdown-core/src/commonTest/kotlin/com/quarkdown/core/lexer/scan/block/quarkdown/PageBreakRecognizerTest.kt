package com.quarkdown.core.lexer.scan.block.quarkdown

import com.quarkdown.core.lexer.scan.LineCursor
import com.quarkdown.core.lexer.scan.Lines
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PageBreakRecognizerTest {
    private fun pageBreak(source: String) =
        PageBreakRecognizer
            .open(LineCursor(Lines.of(source), 0))
            ?.token
            ?.data
            ?.text

    @Test
    fun `covers the marker only`() {
        assertEquals("<<<", pageBreak("<<<\n"))
        assertEquals("<<<<", pageBreak("<<<<\n"))
        assertEquals("<<<", pageBreak("<<<   \n"))
        assertEquals("  <<<", pageBreak("  <<<\n"))
    }

    @Test
    fun `declines short or followed markers`() {
        assertNull(pageBreak("<<\n"))
        assertNull(pageBreak("<<< x\n"))
        assertNull(pageBreak("    <<<\n"))
    }
}
