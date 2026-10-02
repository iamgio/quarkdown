package com.quarkdown.core.lexer.scan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Test that ensures a walk on a function call must cost work proportional to the call only,
 * rather than the entire document (quadratic).
 */
class FunctionCallWalkAccessTest {
    /**
     * A [CharSequence] that counts every character read, copied or materialized through it.
     */
    private class CountingCharSequence(
        private val text: String,
        private val counter: IntArray = IntArray(1),
    ) : CharSequence {
        val touched: Int
            get() = counter[0]

        override val length: Int
            get() = text.length

        override fun get(index: Int): Char {
            counter[0]++
            return text[index]
        }

        override fun subSequence(
            startIndex: Int,
            endIndex: Int,
        ): CharSequence {
            counter[0] += endIndex - startIndex
            return CountingCharSequence(text.substring(startIndex, endIndex), counter)
        }

        override fun toString(): String {
            counter[0] += length
            return text
        }
    }

    private val call = ".box {Title}\n    Body line.\n    Another body line.\n"

    @Test
    fun `walking a block call touches characters proportional to the call, not the document`() {
        val tail = "\nPlain paragraph text that follows the call.\n".repeat(10_000)
        val source = CountingCharSequence(call + tail)

        val result = assertNotNull(walkFunctionCallAt(source, 0))

        assertEquals(call.trimEnd(), result.sourceText.toString().trimEnd())
        assertTrue(
            source.touched < call.length * 50,
            "Touched ${source.touched} characters to walk a ${call.length}-character call in a ${source.length}-character source",
        )
    }
}
