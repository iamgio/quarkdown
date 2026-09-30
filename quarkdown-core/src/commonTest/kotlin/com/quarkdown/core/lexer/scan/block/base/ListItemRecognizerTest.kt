package com.quarkdown.core.lexer.scan.block.base

import com.quarkdown.core.lexer.scan.LineCursor
import com.quarkdown.core.lexer.scan.Lines
import com.quarkdown.core.lexer.tokens.ListItemToken
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ListItemRecognizerTest {
    private fun item(source: String) = ListItemRecognizer.open(LineCursor(Lines.of("$source\n"), 0))?.token as ListItemToken?

    @Test
    fun `reads the marker and the task group`() {
        assertEquals("-", item("- a")!!.marker)
        assertEquals("", item("- a")!!.task)
        assertEquals(" [x]", item("- [x] t")!!.task)
        assertEquals("1.", item("1. a")!!.marker)
        assertEquals("  -", item("  - a")!!.marker)
    }

    @Test
    fun `ends before a following bullet and keeps an indented continuation`() {
        assertEquals("- a", item("- a\n- b")?.data?.text)
        assertEquals("- a\n  cont", item("- a\n  cont\n- b")?.data?.text)
        assertEquals("- a\n\n  b", item("- a\n\n  b\n- c")?.data?.text)
        assertEquals("- a\nlazy\n", item("- a\nlazy")?.data?.text)
    }

    @Test
    fun `keeps an empty item and resumes a task item like a plain one`() {
        assertEquals("- ", item("- \n- b")?.data?.text)
        assertEquals("- [ ] t\n  c\n", item("- [ ] t\n  c")?.data?.text)
        assertEquals("- [ ] a\n\n  b", item("- [ ] a\n\n  b\n- c")?.data?.text)
        assertEquals("- a\n\n  b", item("- a\n\n  b\n- c")?.data?.text)
        assertNull(item("a"))
    }
}
