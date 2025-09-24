package com.quarkdown.core

import com.quarkdown.core.ast.attributes.location.SectionLocation
import com.quarkdown.core.document.numbering.AlphaNumberingSymbol
import com.quarkdown.core.document.numbering.DecimalNumberingSymbol
import com.quarkdown.core.document.numbering.NumberingFixedSymbol
import com.quarkdown.core.document.numbering.NumberingFormat
import com.quarkdown.core.util.StringCase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

/**
 * Tests for [NumberingFormat].
 */
class NumberingFormatTest {
    private fun NumberingFormat.format(
        vararg levels: Int,
        allowMismatchingLength: Boolean = true,
    ) = format(SectionLocation(levels.toList()), allowMismatchingLength)

    @Test
    fun `decimal numbering symbol mapping`() {
        assertEquals("3", DecimalNumberingSymbol.map(3))
    }

    @Test
    fun `alpha numbering symbol lower case mapping`() {
        assertEquals("b", AlphaNumberingSymbol(StringCase.Lower).map(2))
    }

    @Test
    fun `alpha numbering symbol upper case mapping`() {
        assertEquals("C", AlphaNumberingSymbol(StringCase.Upper).map(3))
    }

    @Test
    fun `numbering format parsing from string`() {
        val format = NumberingFormat.fromString("1.1.a-A")

        with(format.symbols.iterator()) {
            assertIs<DecimalNumberingSymbol>(next())
            assertEquals('.', (next() as NumberingFixedSymbol).value)
            assertIs<DecimalNumberingSymbol>(next())
            assertEquals('.', (next() as NumberingFixedSymbol).value)

            next().let {
                assertIs<AlphaNumberingSymbol>(it)
                assertEquals(StringCase.Lower, it.case)
            }

            assertEquals('-', (next() as NumberingFixedSymbol).value)

            next().let {
                assertIs<AlphaNumberingSymbol>(it)
                assertEquals(StringCase.Upper, it.case)
            }
        }
    }

    @Test
    fun `basic numbering formatting`() {
        val format = NumberingFormat.fromString("1.1.a-A")

        assertEquals("1.1.a-A", format.format(1, 1, 1, 1))
        assertEquals("2.2.b-B", format.format(2, 2, 2, 2))
        assertEquals("2.1.c-A", format.format(2, 1, 3, 1))
    }

    @Test
    fun `advanced numbering formatting`() {
        val format = NumberingFormat.fromString("1.1.a-A")

        assertEquals("3.2.d-P", format.format(3, 2, 4, 16))
        assertEquals("12.20.e-A", format.format(12, 20, 5, 1))
        assertEquals("0.0.0-0", format.format(0, 0, 0, 0))
    }

    @Test
    fun `partial level formatting`() {
        val format = NumberingFormat.fromString("1.1.a-A")

        assertEquals("2.1.b", format.format(2, 1, 2))
        assertEquals("1", format.format(1))
    }

    @Test
    fun `excess levels with mismatching length allowed`() {
        val format = NumberingFormat.fromString("1.1.a-A")

        assertEquals("1.2.c-D", format.format(1, 2, 3, 4, 5, 6))
    }

    @Test
    fun `excess levels with mismatching length disallowed`() {
        val format = NumberingFormat.fromString("1.1.a-A")

        assertEquals("", format.format(1, 2, 3, 4, 5, 6, allowMismatchingLength = false))
    }

    @Test
    fun `roman numeral formatting`() {
        val roman = NumberingFormat.fromString("I.i")

        assertEquals("III", roman.format(3))
        assertEquals("I.i", roman.format(1, 1))
        assertEquals("IV.iii", roman.format(4, 3))
        assertEquals("XVII.lvii", roman.format(17, 57))
    }

    @Test
    fun `roman numeral formatting with unallowed values`() {
        val roman = NumberingFormat.fromString("I.i")

        assertEquals("XVII.0", roman.format(17, 0))
        assertEquals("0.50000", roman.format(0, 50000))
    }

    @Test
    fun `trailing fixed symbol`() {
        val format = NumberingFormat.fromString("(1.1)")

        assertEquals("(2.3)", format.format(2, 3))
        assertEquals("(1", format.format(1))
    }
}
