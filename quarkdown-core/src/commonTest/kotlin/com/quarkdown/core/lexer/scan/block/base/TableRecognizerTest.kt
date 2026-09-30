package com.quarkdown.core.lexer.scan.block.base

import com.quarkdown.core.lexer.scan.LineCursor
import com.quarkdown.core.lexer.scan.Lines
import com.quarkdown.core.lexer.scan.block.BaseInterruptions
import com.quarkdown.core.lexer.tokens.TableToken
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TableRecognizerTest {
    private fun line(text: String) = Lines.of("$text\n").first()

    private fun cursor(source: String) = LineCursor(Lines.of("$source\n"), 0)

    private val recognizer = TableRecognizer(BaseInterruptions.tableRows)

    private fun table(source: String) = recognizer.open(LineCursor(Lines.of("$source\n"), 0))?.token as TableToken?

    @Test
    fun `splits header, alignment and rows`() {
        val full = table("|a|b|\n|:-|-:|\n|1|2|")!!
        assertEquals("|a|b|", full.header)
        assertEquals("|:-|-:|", full.alignment)
        assertEquals("|1|2|", full.rows)
    }

    @Test
    fun `accepts a table with no rows and keeps the metadata row`() {
        assertEquals("", table("|a|b|\n|-|-|")!!.rows)
        assertEquals("|1|2|\n\"cap\" {#id}", table("|a|b|\n|-|-|\n|1|2|\n\"cap\" {#id}")!!.rows)
    }

    @Test
    fun `stops at a blank line or an interruption and declines non-tables`() {
        assertEquals("|1|2|", table("|a|b|\n|-|-|\n|1|2|\n\n|3|4|")!!.rows)
        assertEquals("|1|2|", table("|a|b|\n|-|-|\n|1|2|\n# h")!!.rows)
        assertNull(table("|a|b|\n|x|y|"))
        assertNull(table(" \n|-|-|"))
    }

    @Test
    fun `tells an alignment row from a lookalike`() {
        listOf("|-|-|", "|:-|-:|", "| :---: |", "---", "|:-:|:-:|")
            .forEach { assertTrue(TableRecognizer.isAlignmentRow(line(it)), it) }
        listOf("|a|b|", "|-|x|", "", "|", "|-|-| x", "|:|:|")
            .forEach { assertFalse(TableRecognizer.isAlignmentRow(line(it)), it) }
    }

    @Test
    fun `opens only on a header followed by an alignment row`() {
        assertTrue(TableRecognizer.opensAt(cursor("|a|b|\n|-|-|")))
        assertFalse(TableRecognizer.opensAt(cursor("|a|b|\n|x|y|")))
        assertFalse(TableRecognizer.opensAt(cursor(" \n|-|-|")))
    }
}
