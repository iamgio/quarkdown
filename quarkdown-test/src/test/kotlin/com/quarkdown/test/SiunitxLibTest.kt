package com.quarkdown.test

import com.quarkdown.test.util.execute
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

private const val LIBRARY = "latex-siunitx"

/**
 * Tests for the `latex-siunitx` library.
 */
class SiunitxLibTest {
    private fun executeWithLibrary(
        source: String = "",
        block: (Map<String, String>, CharSequence) -> Unit,
    ) = execute(
        ".include {$LIBRARY}\n$source",
        loadableLibraries = setOf(LIBRARY),
    ) {
        block(documentInfo.tex.macros, it)
    }

    @Test
    fun commands() {
        executeWithLibrary { macros, _ ->
            assertEquals("#1", macros["\\num"])
            assertEquals("\\mathrm{\\!#1}", macros["\\unit"])
            assertEquals("\\unit{#1}", macros["\\si"])
            assertEquals("{#1\\,\\unit{#2}}", macros["\\qty"])
            assertEquals("\\qty{#1}{#2}", macros["\\SI"])
            assertEquals("#1^{\\circ}", macros["\\ang"])
            assertEquals("\\qty{#1}{#3}\\text{--}\\qty{#2}{#3}", macros["\\qtyrange"])
        }
    }

    @Test
    fun `powers and qualifiers`() {
        executeWithLibrary { macros, _ ->
            assertEquals("/\\!", macros["\\per"])
            assertEquals("^{2}", macros["\\squared"])
            assertEquals("^{3}", macros["\\cubed"])
            assertEquals("^{#1}", macros["\\tothe"])
            assertEquals("#2^{#1}", macros["\\raiseto"])
        }
    }

    @Test
    fun prefixes() {
        executeWithLibrary { macros, _ ->
            assertEquals("\\,\\mathrm{k}\\!", macros["\\kilo"])
            assertEquals("\\,\\mathrm{\\mu}\\!", macros["\\micro"])
            assertEquals("\\,\\mathrm{da}\\!", macros["\\deca"])
            assertEquals("\\,\\mathrm{Q}\\!", macros["\\quetta"])
        }
    }

    @Test
    fun units() {
        executeWithLibrary { macros, _ ->
            assertEquals("\\,\\mathrm{m}", macros["\\metre"])
            assertEquals("\\metre", macros["\\meter"])
            assertEquals("\\,\\mathrm{kg}", macros["\\kilogram"])
            assertEquals("\\,\\mathrm{\\Omega}", macros["\\ohm"])
            assertEquals("\\,{}^{\\circ}\\mathrm{C}", macros["\\degreeCelsius"])
            assertEquals("'", macros["\\arcminute"])
            assertEquals("\\,\\%", macros["\\percent"])
        }
    }

    @Test
    fun `does not override built-in KaTeX symbols`() {
        executeWithLibrary { macros, _ ->
            assertNull(macros["\\degree"])
            assertNull(macros["\\square"])
        }
    }

    @Test
    fun `usage in formula`() {
        executeWithLibrary("Speed: $ \\SI{3}{\\kilo\\meter\\per\\second} $") { _, output ->
            assertEquals(
                "<p>Speed: <formula>\\SI{3}{\\kilo\\meter\\per\\second}</formula></p>",
                output,
            )
        }
    }
}
