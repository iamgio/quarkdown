package com.quarkdown.interaction.os

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests the default of `--pdf-no-sandbox`, which used to be set by the launcher script
 * and is now decided in Kotlin so that a native binary behaves identically.
 */
class ChromeSandboxTest {
    @Test
    fun `sandbox is disabled by default on Linux`() {
        assertTrue(isChromeSandboxUnavailableOn(OsFamily.LINUX))
    }

    @Test
    fun `sandbox stays enabled by default elsewhere`() {
        listOf(OsFamily.MACOS, OsFamily.WINDOWS, OsFamily.OTHER).forEach {
            assertFalse(isChromeSandboxUnavailableOn(it))
        }
    }
}
