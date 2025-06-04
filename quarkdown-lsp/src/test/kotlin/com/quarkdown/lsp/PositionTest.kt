package com.quarkdown.lsp

import com.quarkdown.lsp.pattern.QuarkdownPatterns
import com.quarkdown.lsp.util.getByPatternContaining
import com.quarkdown.lsp.util.getChar
import org.eclipse.lsp4j.Position
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Tests for the LSP4J Position utility functions.
 */
class PositionTest {
    @Test
    fun `get char`() {
        val text = "Hello\nWorld"
        val position = Position(1, 3)
        val char = position.getChar(text)
        assert(char == 'r')
    }

    @Test
    fun `get function call name, beginning of the word`() {
        val text = "hello .funcall {x} world"
        val position = Position(0, 8) // Position of 'f' in 'funcall'
        val functionCallName =
            position.getByPatternContaining(
                pattern = QuarkdownPatterns.FunctionCall.identifierInCall,
                text = text,
            )
        assertEquals("funcall", functionCallName)
    }

    @Test
    fun `get function call name, mid-word`() {
        val text = "hello .funcall {x} world"
        val position = Position(0, 10) // Position of 'n' in 'funcall'
        val functionCallName =
            position.getByPatternContaining(
                pattern = QuarkdownPatterns.FunctionCall.identifierInCall,
                text = text,
            )
        assertEquals("funcall", functionCallName)
    }

    @Test
    fun `get function call name, not in function call`() {
        val text = "hello .funcall {x} world"
        val position = Position(0, 5) // Position of 'o' in 'hello'
        val functionCallName =
            position.getByPatternContaining(
                pattern = QuarkdownPatterns.FunctionCall.identifierInCall,
                text = text,
            )
        assertNull(functionCallName)
    }
}
