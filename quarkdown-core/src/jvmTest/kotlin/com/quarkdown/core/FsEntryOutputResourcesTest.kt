package com.quarkdown.core

import com.quarkdown.core.filesystem.VirtualFileSystem
import com.quarkdown.core.pipeline.output.FileReferenceOutputArtifact
import com.quarkdown.core.pipeline.output.toOutputResources
import com.quarkdown.core.pipeline.output.visitor.FileResourceExporter
import java.io.File
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for [toOutputResources], which exports every file under a directory at its relative path.
 */
class FsEntryOutputResourcesTest {
    private val fs =
        VirtualFileSystem().apply {
            writeBytes("sections/experience.qd", "# Experience".encodeToByteArray())
            writeBytes("sections/assets/My Photo.png", "png".encodeToByteArray())
            writeBytes(".gitignore", "output/".encodeToByteArray())
        }

    @Test
    fun `one file reference per file, named by its relative path`() {
        val resources = fs.resolve("/").toOutputResources().filterIsInstance<FileReferenceOutputArtifact>()
        assertEquals(
            setOf("sections/experience.qd", "sections/assets/My Photo.png", ".gitignore"),
            resources.map { it.name }.toSet(),
        )
        assertEquals("output/", resources.first { it.name == ".gitignore" }.file.readText())
    }

    @Test
    fun `empty directory yields no resources`() {
        assertEquals(emptySet(), VirtualFileSystem().resolve("/").toOutputResources())
    }

    @Test
    fun `exported files keep their exact names and nesting`() {
        val dir = Files.createTempDirectory("fsEntryOutputResources").toFile()
        try {
            fs.resolve("/").toOutputResources().forEach { it.accept(FileResourceExporter(dir)) }
            assertEquals("output/", File(dir, ".gitignore").readText())
            assertEquals("png", File(dir, "sections/assets/My Photo.png").readText())
            assertEquals("# Experience", File(dir, "sections/experience.qd").readText())
        } finally {
            dir.deleteRecursively()
        }
    }
}
