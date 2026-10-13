package com.quarkdown.pm.install

import com.quarkdown.core.filesystem.VirtualFileSystem
import com.quarkdown.pm.model.AtomManifest
import com.quarkdown.pm.model.PackageFile
import com.quarkdown.pm.model.PackageManifest
import com.quarkdown.pm.model.RepositoryPackage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for [VirtualFileSystemPackageWriter].
 */
class VirtualFileSystemPackageWriterTest {
    private val fs = VirtualFileSystem("/project")
    private val target = fs.resolve("/project/@atoms/cv")
    private val manifest = PackageManifest(AtomManifest("cv", "1.0.0", "main.qd"))
    private val png = byteArrayOf(0x89.toByte(), 0x50, 0xFF.toByte())

    private fun pkg(vararg files: Pair<String, ByteArray>) =
        RepositoryPackage(manifest, files.map { (path, bytes) -> PackageFile(path) { bytes } })

    @Test
    fun `writes nested text and binary files under the target`() {
        VirtualFileSystemPackageWriter(fs).write(pkg("main.qd" to "# CV".encodeToByteArray(), "assets/logo.png" to png), target)
        assertEquals("# CV", target.resolve("main.qd").readText())
        assertEquals(png.toList(), target.resolve("assets/logo.png").readBytes().toList())
    }

    @Test
    fun `replaces an existing installation entirely`() {
        fs.write("/project/@atoms/cv/stale.qd", "old")
        VirtualFileSystemPackageWriter(fs).write(pkg("main.qd" to "new".encodeToByteArray()), target)
        assertFalse(target.resolve("stale.qd").exists)
        assertTrue(target.resolve("main.qd").exists)
    }

    @Test
    fun `empty package yields an existing empty directory`() {
        VirtualFileSystemPackageWriter(fs).write(pkg(), target)
        assertTrue(target.isDirectory)
        assertEquals(emptyList(), target.children())
    }
}
