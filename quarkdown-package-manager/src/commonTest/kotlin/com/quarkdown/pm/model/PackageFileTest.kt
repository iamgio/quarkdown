package com.quarkdown.pm.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for [PackageFile] path validation.
 */
class PackageFileTest {
    @Test
    fun `accepts relative paths with nested directories`() {
        assertEquals("assets/img/logo.png", PackageFile("assets/img/logo.png") { byteArrayOf() }.path)
        assertEquals(".gitignore", PackageFile(".gitignore") { byteArrayOf() }.path)
    }

    @Test
    fun `internal paths are those with a segment starting with a double underscore`() {
        listOf("__tests/main.qd", "a/__tests/x", "__notes.md", "assets/__draft.png").forEach { path ->
            assertTrue(PackageFile.isInternal(path), path)
        }
        listOf("main.qd", "tests/main.qd", "a_b/c__", "_single.md").forEach { path ->
            assertFalse(PackageFile.isInternal(path), path)
        }
    }

    @Test
    fun `rejects paths that could escape the package root`() {
        listOf("", "/etc/passwd", "../x.qd", "a/../../x.qd", "a/..", "..", "a\\..\\x.qd", "dir\\file.qd", "C:/x.qd").forEach { invalid ->
            assertFailsWith<IllegalArgumentException>(invalid) { PackageFile(invalid) { byteArrayOf() } }
        }
    }
}
