package com.quarkdown.interaction.os

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for [OsUtils] family detection.
 *
 * Detection is driven by the `os.name` system property, so the family resolution is tested
 * against representative values rather than against the host the suite happens to run on.
 */
class OsUtilsTest {
    @Test
    fun `windows names resolve to the windows family`() {
        listOf("Windows 10", "Windows Server 2022", "windows 11").forEach {
            assertEquals(OsFamily.WINDOWS, OsFamily.of(it), "for os.name='$it'")
        }
    }

    @Test
    fun `mac names resolve to the macos family`() {
        listOf("Mac OS X", "macOS", "Darwin").forEach {
            assertEquals(OsFamily.MACOS, OsFamily.of(it), "for os.name='$it'")
        }
    }

    @Test
    fun `linux names resolve to the linux family`() {
        listOf("Linux", "linux", "LINUX").forEach {
            assertEquals(OsFamily.LINUX, OsFamily.of(it), "for os.name='$it'")
        }
    }

    @Test
    fun `other unix names resolve to the other family`() {
        listOf("SunOS", "FreeBSD", "AIX").forEach {
            assertEquals(OsFamily.OTHER, OsFamily.of(it), "for os.name='$it'")
        }
    }

    @Test
    fun `macos is not mistaken for linux`() {
        assertFalse(OsFamily.of("Mac OS X") == OsFamily.LINUX)
    }

    @Test
    fun `current family is one of the known values`() {
        assertTrue(OsUtils.family in OsFamily.entries)
    }
}
