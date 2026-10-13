package com.quarkdown.pm.layout

import com.quarkdown.core.filesystem.VirtualFileSystem
import com.quarkdown.pm.model.PackageIdentifier
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for [PackageLayout].
 */
class PackageLayoutTest {
    private val root = VirtualFileSystem("/project").resolve("/project")

    @Test
    fun `package directory is project root slash at-scope slash name`() {
        val directory = PackageLayout.directoryOf(root, PackageIdentifier("atoms", "cv"))
        assertEquals("/project/@atoms/cv", directory.invariantSeparatorsPath)
    }

    @Test
    fun `manifest sits at the package root`() {
        val directory = PackageLayout.directoryOf(root, PackageIdentifier("atoms", "cv"))
        assertEquals("/project/@atoms/cv/atom.toml", PackageLayout.manifestOf(directory).invariantSeparatorsPath)
    }

    @Test
    fun `paths under a top-level at-directory are package paths`() {
        listOf("@atoms/cv/main.qd", "@atoms/cv", "@atoms/.cv.tmp/x").forEach { path ->
            assertTrue(PackageLayout.isPackagePath(root, root.resolve(path)), path)
        }
    }

    @Test
    fun `other paths are not package paths`() {
        val fs = VirtualFileSystem("/project")
        listOf("chapters/@intro.qd", "main.qd", "@notes.qd").forEach { path ->
            assertFalse(PackageLayout.isPackagePath(root, root.resolve(path)), path)
        }
        assertFalse(PackageLayout.isPackagePath(root, root))
        assertFalse(PackageLayout.isPackagePath(root, fs.resolve("/elsewhere/@atoms/x")))
    }
}
