package com.quarkdown.core.filesystem

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for [FsPaths] raw path string checks.
 */
class FsPathsTest {
    @Test
    fun `unix paths`() {
        assertTrue(FsPaths.isAbsolute("/absolute/path"))
        assertFalse(FsPaths.isAbsolute("relative/path"))
        assertFalse(FsPaths.isAbsolute("./relative"))
        assertFalse(FsPaths.isAbsolute("../relative"))
    }

    @Test
    fun `windows drive-letter paths`() {
        assertTrue(FsPaths.isAbsolute("C:\\absolute\\path"))
        assertTrue(FsPaths.isAbsolute("C:/absolute/path"))
        assertFalse(FsPaths.isAbsolute("relative\\path"))
    }
}
