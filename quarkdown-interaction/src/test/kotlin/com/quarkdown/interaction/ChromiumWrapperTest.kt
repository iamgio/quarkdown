package com.quarkdown.interaction

import com.quarkdown.interaction.executable.ChromiumWrapper
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse

/**
 * Tests for [ChromiumWrapper].
 */
class ChromiumWrapperTest {
    @Test
    fun `blank path is rejected`() {
        assertFailsWith<IllegalArgumentException> { ChromiumWrapper(path = " ") }
    }

    @Test
    fun `nonexistent executable is invalid`() {
        assertFalse(ChromiumWrapper(path = "not-a-browser-executable").isValid)
    }

    @Test
    fun `default path falls back to headless shell name`() {
        if (System.getenv(Env.QUARKDOWN_CHROME_PATH) == null) {
            assertEquals("chrome-headless-shell", ChromiumWrapper.defaultPath)
        }
    }
}
