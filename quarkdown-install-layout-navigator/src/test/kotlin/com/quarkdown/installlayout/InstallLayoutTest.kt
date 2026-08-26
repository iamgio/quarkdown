package com.quarkdown.installlayout

import com.quarkdown.core.filesystem.DiskFileSystem
import com.quarkdown.core.filesystem.VirtualFileSystem
import com.quarkdown.core.pipeline.output.BinaryOutputArtifact
import com.quarkdown.core.pipeline.output.FileReferenceOutputArtifact
import com.quarkdown.core.pipeline.output.OutputResourceGroup
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Tests for [InstallLayout] navigation over both virtual and disk-backed file systems.
 */
class InstallLayoutTest {
    /**
     * @return an [InstallLayout] over an in-memory file system seeded with a minimal install layout
     */
    private fun virtualLayout(): InstallLayout {
        val fs = VirtualFileSystem("/install/lib")
        fs.write("html/theme/global.css", "body {}")
        fs.write("html/theme/layout/latex/latex.css", ".latex {}")
        fs.write("html/script/quarkdown.min.js", "// runtime")
        fs.write("qd/stdlib.qd", "")
        fs.write("skills/quarkdown/SKILL.md", "# Skill")
        return InstallLayout(InstallLayoutDirectory(fs.resolve("/install/lib")))
    }

    @Test
    fun `navigates a virtual layout`() {
        val layout = virtualLayout()
        assertTrue(layout.quarkdownLibraries.exists())
        assertTrue(layout.agentSkill.exists())
        assertTrue(layout.htmlResources.scripts.exists())
        assertTrue(
            layout.htmlResources.themes.global
                .exists(),
        )
        assertTrue(
            layout.htmlResources.themes.layout
                .resolveDirectory("latex")
                .exists(),
        )
    }

    @Test
    fun `existence is type-checked`() {
        val layout = virtualLayout()
        // A file entry pointing at a directory does not exist as a file, and vice versa.
        assertFalse(layout.resolveFile("qd").exists())
        assertFalse(layout.resolveDirectory("html/theme/global.css").exists())
        assertFalse(layout.resolveDirectory("nonexistent").exists())
    }

    @Test
    fun `virtual entry materializes into in-memory resources`() {
        val scripts = virtualLayout().htmlResources.scripts
        val resource = assertIs<OutputResourceGroup>(scripts.asOutputResource())
        assertEquals("script", resource.name)
        val artifact = assertIs<BinaryOutputArtifact>(resource.resources.single())
        assertEquals("quarkdown.min.js", artifact.name)
        assertEquals("// runtime", artifact.content.toByteArray().decodeToString())
    }

    @Test
    fun `disk entry is referenced by file`() {
        val directory = Files.createTempDirectory("quarkdown-install-layout").toFile()
        try {
            directory.resolve("qd").mkdirs()
            val layout = InstallLayout(InstallLayoutDirectory(DiskFileSystem().resolve(directory.absolutePath)))
            assertTrue(layout.quarkdownLibraries.exists())
            val resource = assertIs<FileReferenceOutputArtifact>(layout.quarkdownLibraries.asOutputResource())
            assertEquals(directory.resolve("qd"), resource.file)
        } finally {
            directory.deleteRecursively()
        }
    }
}
