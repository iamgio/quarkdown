package com.quarkdown.pm.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for [RepositoryPackage.download].
 */
class RepositoryPackageTest {
    private val manifest = PackageManifest(AtomManifest(name = "cv", version = "1.0.0", entrypoint = "main.qd"))

    private fun file(
        path: String,
        bytes: ByteArray,
    ) = PackageFile(path) { bytes }

    @Test
    fun `download writes every file at its relative path`() {
        val png = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0xFF.toByte(), 0xFE.toByte())
        val pkg =
            RepositoryPackage(
                manifest,
                listOf(
                    file("atom.toml", "[atom]".encodeToByteArray()),
                    file("main.qd", "# CV".encodeToByteArray()),
                    file("assets/img/logo.png", png),
                    file(".gitignore", "output".encodeToByteArray()),
                ),
            )
        val fs = pkg.download()
        assertEquals("# CV", fs.resolve("main.qd").readText())
        assertEquals("[atom]", fs.resolve("atom.toml").readText())
        assertEquals(png.toList(), fs.resolve("assets/img/logo.png").readBytes().toList())
        assertEquals("output", fs.resolve(".gitignore").readText())
        assertTrue(fs.resolve("assets/img").isDirectory)
    }

    @Test
    fun `download of empty package yields empty root`() {
        val fs = RepositoryPackage(manifest, emptyList()).download()
        assertEquals(emptyList(), fs.resolve("/").children())
    }
}
