package com.quarkdown.core.lexer.scan.block

import com.quarkdown.core.lexer.scan.LineCursor
import com.quarkdown.core.lexer.scan.Lines
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class InterruptionTest {
    private fun cursor(source: String) = LineCursor(Lines.of("$source\n"), 0)

    @Test
    fun `paragraph interruptions accept only the first ordered bullet`() {
        assertTrue(BaseInterruptions.paragraph.interrupts(cursor("1. x")))
        assertFalse(BaseInterruptions.paragraph.interrupts(cursor("10. x")))
        assertTrue(BaseInterruptions.paragraph.interrupts(cursor("- x")))
    }

    @Test
    fun `list interruptions accept any ordered bullet`() {
        assertTrue(BaseInterruptions.list.interrupts(cursor("7. x")))
        assertTrue(BaseInterruptions.list.interrupts(cursor("10. x")))
        assertFalse(BaseInterruptions.list.interrupts(cursor("1234567890. x")))
    }

    @Test
    fun `table rows are not interrupted by another table`() {
        assertTrue(BaseInterruptions.paragraph.interrupts(cursor("|a|b|\n|-|-|")))
        assertFalse(BaseInterruptions.tableRows.interrupts(cursor("|a|b|\n|-|-|")))
    }

    @Test
    fun `quarkdown adds multiline math`() {
        assertFalse(BaseInterruptions.paragraph.interrupts(cursor("$$$")))
        assertTrue(QuarkdownInterruptions.paragraph.interrupts(cursor("$$$")))
    }

    @Test
    fun `shared rules interrupt in every variant`() {
        listOf("---", "### h", "#! d", "> q", "```", " ", "\t").forEach {
            assertTrue(BaseInterruptions.paragraph.interrupts(cursor(it)), it)
            assertTrue(BaseInterruptions.tableRows.interrupts(cursor(it)), it)
            assertTrue(BaseInterruptions.list.interrupts(cursor(it)), it)
        }
        assertFalse(BaseInterruptions.paragraph.interrupts(cursor("plain text")))
    }
}
