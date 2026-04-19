package com.quarkdown.test

import com.quarkdown.test.util.execute
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Integration tests for `.keybinding` function call rendering.
 */
class KeybindingTest {
    @Test
    fun `single key`() {
        execute("Press .keybinding {K} to continue.") {
            assertEquals(
                "<p>Press <span class=\"keybinding\">" +
                    "<kbd>K</kbd>" +
                    "</span> to continue.</p>",
                it,
            )
        }
    }

    @Test
    fun `modifier combination`() {
        execute("Press .keybinding {Cmd+Shift+K} to delete.") {
            assertEquals(
                "<p>Press <span class=\"keybinding\">" +
                    "<kbd data-mac=\"⌘\">Ctrl</kbd>" +
                    "<kbd data-mac=\"⇧\">Shift</kbd>" +
                    "<kbd>K</kbd>" +
                    "</span> to delete.</p>",
                it,
            )
        }
    }
}
