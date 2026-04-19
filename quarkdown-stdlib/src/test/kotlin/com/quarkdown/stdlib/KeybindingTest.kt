package com.quarkdown.stdlib

import com.quarkdown.core.ast.quarkdown.inline.Keybinding
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Tests for [keybinding] parsing.
 */
class KeybindingTest {
    private fun parse(input: String) = keybinding(input).unwrappedValue as Keybinding

    @Test
    fun `single key`() {
        val kb = parse("K")
        assertEquals(listOf(Keybinding.Key("K")), kb.parts)
    }

    @Test
    fun `plus separator`() {
        val kb = parse("Ctrl+K")
        assertEquals(
            listOf(Keybinding.CtrlModifier, Keybinding.Key("K")),
            kb.parts,
        )
    }

    @Test
    fun `comma separator`() {
        val kb = parse("Alt,F4")
        assertEquals(
            listOf(Keybinding.AltModifier, Keybinding.Key("F4")),
            kb.parts,
        )
    }

    @Test
    fun `dash separator`() {
        val kb = parse("Shift-A")
        assertEquals(
            listOf(Keybinding.ShiftModifier, Keybinding.Key("A")),
            kb.parts,
        )
    }

    @Test
    fun `primary modifier aliases`() {
        for (alias in listOf("cmd", "command", "meta", "mod", "Cmd", "CMD")) {
            val kb = parse(alias)
            assertEquals(listOf(Keybinding.PrimaryModifier), kb.parts)
        }
    }

    @Test
    fun `ctrl aliases`() {
        for (alias in listOf("ctrl", "control", "Ctrl", "CONTROL")) {
            val kb = parse(alias)
            assertEquals(listOf(Keybinding.CtrlModifier), kb.parts)
        }
    }

    @Test
    fun `alt aliases`() {
        for (alias in listOf("alt", "option", "Alt", "Option")) {
            val kb = parse(alias)
            assertEquals(listOf(Keybinding.AltModifier), kb.parts)
        }
    }

    @Test
    fun `full combination`() {
        val kb = parse("Cmd+Shift+K")
        assertEquals(
            listOf(Keybinding.PrimaryModifier, Keybinding.ShiftModifier, Keybinding.Key("K")),
            kb.parts,
        )
    }

    @Test
    fun `whitespace is trimmed`() {
        val kb = parse("Ctrl + Shift + K")
        assertEquals(
            listOf(Keybinding.CtrlModifier, Keybinding.ShiftModifier, Keybinding.Key("K")),
            kb.parts,
        )
    }

    @Test
    fun `keys are capitalized`() {
        val kb = parse("a")
        assertEquals(listOf(Keybinding.Key("A")), kb.parts)
    }

    @Test
    fun `literal plus`() {
        val kb = parse("Ctrl+plus")
        assertEquals(
            listOf(Keybinding.CtrlModifier, Keybinding.Key("+")),
            kb.parts,
        )
    }

    @Test
    fun `literal comma`() {
        val kb = parse("Ctrl+comma")
        assertEquals(
            listOf(Keybinding.CtrlModifier, Keybinding.Key(",")),
            kb.parts,
        )
    }

    @Test
    fun `literal dash`() {
        val kb = parse("Ctrl+dash")
        assertEquals(
            listOf(Keybinding.CtrlModifier, Keybinding.Key("-")),
            kb.parts,
        )
    }

    @Test
    fun `literal dot`() {
        val kb = parse("Ctrl+dot")
        assertEquals(
            listOf(Keybinding.CtrlModifier, Keybinding.Key(".")),
            kb.parts,
        )
    }

    @Test
    fun `empty input throws`() {
        assertFailsWith<IllegalArgumentException> {
            parse("")
        }
    }

    @Test
    fun `blank input throws`() {
        assertFailsWith<IllegalArgumentException> {
            parse("  +  ")
        }
    }
}
