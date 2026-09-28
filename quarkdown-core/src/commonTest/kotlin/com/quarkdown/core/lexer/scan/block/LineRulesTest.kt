package com.quarkdown.core.lexer.scan.block

import com.quarkdown.core.lexer.scan.Lines
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LineRulesTest {
    private fun line(text: String) = Lines.of("$text\n").first()

    @Test
    fun `applies the two ordered bullet policies`() {
        assertEquals(1, LineRules.bulletLengthOf(line("- a"), OrderedBulletPolicy.ONE_ONLY))
        assertEquals(2, LineRules.bulletLengthOf(line("1. a"), OrderedBulletPolicy.ONE_ONLY))
        assertNull(LineRules.bulletLengthOf(line("2. a"), OrderedBulletPolicy.ONE_ONLY))
        assertEquals(2, LineRules.bulletLengthOf(line("1) a"), OrderedBulletPolicy.ONE_ONLY))
        assertNull(LineRules.bulletLengthOf(line("1- a"), OrderedBulletPolicy.ONE_ONLY))
        assertEquals(3, LineRules.bulletLengthOf(line("10. a"), OrderedBulletPolicy.UP_TO_NINE_DIGITS))
        assertNull(LineRules.bulletLengthOf(line("1234567890. a"), OrderedBulletPolicy.UP_TO_NINE_DIGITS))
        assertTrue(LineRules.isBulletInterruption(line("- a"), OrderedBulletPolicy.ONE_ONLY))
        assertFalse(LineRules.isBulletInterruption(line("-\ta"), OrderedBulletPolicy.ONE_ONLY))
    }

    @Test
    fun `counts only ascii whitespace as blankness`() {
        assertFalse(LineRules.isBlankTerminatedLine(line("\u00a0")))
    }

    @Test
    fun `accepts any blank terminated line as an interruption`() {
        assertTrue(LineRules.isBlankTerminatedLine(line(" ")))
        assertTrue(LineRules.isBlankTerminatedLine(line("   ")))
        assertTrue(LineRules.isBlankTerminatedLine(line("\t")))
        assertFalse(LineRules.isBlankTerminatedLine(line("")))
        assertFalse(LineRules.isBlankTerminatedLine(line("x")))
    }
}
