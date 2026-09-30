package com.quarkdown.core.lexer.scan.inline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DelimiterRunTest {
    private fun run(
        source: String,
        index: Int,
    ) = DelimiterRuns.classify(source, index, source[index])

    @Test
    fun `counts runs and finds line starts`() {
        assertEquals(3, DelimiterRuns.runLengthOf("***a", 0, '*'))
        assertEquals(0, DelimiterRuns.runLengthOf("a***", 0, '*'))
        assertTrue(DelimiterRuns.isLineStart("x\n*a*", 2))
        assertFalse(DelimiterRuns.isLineStart("x*a*", 1))
    }

    @Test
    fun `classifies asterisk runs by flanking`() {
        assertTrue(run("*a*", 0)!!.canOpen)
        assertFalse(run("*a*", 0)!!.canClose)
        assertTrue(run("*a*", 2)!!.canClose)
        assertPairsNothing(run("* a *", 0)!!)
        assertEquals(3, run("***a", 0)!!.originalLength)
    }

    @Test
    fun `applies the intraword rule to underscores only`() {
        assertTrue(run("a*b*c", 1)!!.canOpen)
        assertFalse(run("a_b_c", 1)!!.canOpen)
        assertFalse(run("a_b_c", 3)!!.canClose)
        assertTrue(run("_a_", 0)!!.canOpen)
        assertTrue(run("(_a_)", 1)!!.canOpen)
    }

    @Test
    fun `pairs only double tildes and flags a run that does neither`() {
        assertTrue(run("~~a~~", 0)!!.canOpen)
        assertPairsNothing(run("~a~", 0)!!)
        assertPairsNothing(run("~~~a~~~", 0)!!)
        assertEquals(3, run("~~~a~~~", 0)!!.originalLength)
        assertPairsNothing(run("a * b", 2)!!)
        assertNull(DelimiterRuns.classify("a * b", 0, '*'))
    }

    private fun assertPairsNothing(run: DelimiterRun) {
        assertFalse(run.canOpen)
        assertFalse(run.canClose)
    }

    @Test
    fun `treats non-ascii punctuation as punctuation`() {
        assertTrue(run("—_a_", 1)!!.canOpen)
        assertFalse(run("é_a_", 2)!!.canOpen)
    }
}
