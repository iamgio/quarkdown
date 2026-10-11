package com.quarkdown.pm.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

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
    fun `rejects paths that could escape the package root`() {
        listOf("", "/etc/passwd", "../x.qd", "a/../../x.qd", "a/..", "..", "a\\..\\x.qd", "dir\\file.qd", "C:/x.qd").forEach { invalid ->
            assertFailsWith<IllegalArgumentException>(invalid) { PackageFile(invalid) { byteArrayOf() } }
        }
    }
}
