package com.quarkdown.core

import com.quarkdown.core.pipeline.output.ArtifactType
import com.quarkdown.core.pipeline.output.BinaryOutputArtifact
import com.quarkdown.core.pipeline.output.FileReferenceOutputArtifact
import com.quarkdown.core.pipeline.output.OutputResourceGroup
import com.quarkdown.core.pipeline.output.TextOutputArtifact
import com.quarkdown.core.pipeline.output.visitor.FileResourceExporter
import java.io.File
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for [FileResourceExporter] writing resources to disk.
 */
class FileResourceExporterTest {
    private fun withTempDir(block: (File) -> Unit) {
        val dir = Files.createTempDirectory("exporterTest").toFile()
        try {
            block(dir)
        } finally {
            dir.deleteRecursively()
        }
    }

    // Text artifacts

    @Test
    fun `text artifact is written with correct extension`() =
        withTempDir { dir ->
            val artifact = TextOutputArtifact(name = "index", content = "<h1>hello</h1>", type = ArtifactType.HTML)
            val result = artifact.accept(FileResourceExporter(dir))

            assertEquals("index.html", result.name)
            assertEquals("<h1>hello</h1>", result.readText())
        }

    @Test
    fun `auto artifact preserves original name`() =
        withTempDir { dir ->
            val artifact = TextOutputArtifact(name = "data.csv", content = "a,b", type = ArtifactType.AUTO)
            val result = artifact.accept(FileResourceExporter(dir))

            assertEquals("data.csv", result.name)
            assertEquals("a,b", result.readText())
        }

    // Binary artifacts

    @Test
    fun `binary artifact is written`() =
        withTempDir { dir ->
            val bytes = listOf<Byte>(0x00, 0x01, 0x02)
            val artifact = BinaryOutputArtifact(name = "out", content = bytes, type = ArtifactType.AUTO)
            val result = artifact.accept(FileResourceExporter(dir))

            assertEquals(bytes, result.readBytes().toList())
        }

    // File reference artifacts

    @Test
    fun `file reference copies a single file`() =
        withTempDir { dir ->
            val source = File(dir, "src").also { it.mkdir() }
            val sourceFile = File(source, "style.css").also { it.writeText("body {}") }

            val output = File(dir, "out").also { it.mkdir() }
            val artifact = FileReferenceOutputArtifact(name = "style.css", file = sourceFile)
            val result = artifact.accept(FileResourceExporter(output))

            assertEquals("style.css", result.name)
            assertEquals("body {}", result.readText())
        }

    @Test
    fun `file reference copies a directory recursively`() =
        withTempDir { dir ->
            val source = File(dir, "lib").also { it.mkdir() }
            File(source, "a.js").writeText("var a;")
            File(source, "sub").mkdir()
            File(source, "sub/b.js").writeText("var b;")

            val output = File(dir, "out").also { it.mkdir() }
            val artifact = FileReferenceOutputArtifact(name = "lib", file = source)
            val result = artifact.accept(FileResourceExporter(output))

            assertTrue(result.isDirectory)
            assertEquals("var a;", File(result, "a.js").readText())
            assertEquals("var b;", File(result, "sub/b.js").readText())
        }

    // Resource groups

    @Test
    fun `group creates a subdirectory with nested resources`() =
        withTempDir { dir ->
            val group =
                OutputResourceGroup(
                    name = "theme",
                    resources =
                        setOf(
                            TextOutputArtifact(name = "global", content = "* {}", type = ArtifactType.CSS),
                            TextOutputArtifact(name = "dark", content = ".dark {}", type = ArtifactType.CSS),
                        ),
                )
            val result = group.accept(FileResourceExporter(dir))

            assertTrue(result.isDirectory)
            assertEquals("theme", result.name)
            assertEquals("* {}", File(result, "global.css").readText())
            assertEquals(".dark {}", File(result, "dark.css").readText())
        }

    @Test
    fun `empty group does not create directory`() =
        withTempDir { dir ->
            val group = OutputResourceGroup(name = "empty", resources = emptySet())
            val result = group.accept(FileResourceExporter(dir))

            assertFalse(result.exists())
        }

    // write = false

    @Test
    fun `write false returns paths without creating files`() =
        withTempDir { dir ->
            val artifact = TextOutputArtifact(name = "ghost", content = "boo", type = ArtifactType.HTML)
            val result = artifact.accept(FileResourceExporter(dir, write = false))

            assertEquals("ghost.html", result.name)
            assertFalse(result.exists())
        }

    // Checksum invalidation

    @Test
    fun `checksum skips copy when source unchanged`() =
        withTempDir { dir ->
            val source = File(dir, "src").also { it.mkdir() }
            File(source, "lib.js").writeText("console.log('v1');")

            val output = File(dir, "out").also { it.mkdir() }
            val artifact = FileReferenceOutputArtifact(name = "lib.js", file = File(source, "lib.js"), useChecksumInvalidation = true)

            // First export: file is copied, checksum is written.
            artifact.accept(FileResourceExporter(output))
            val copied = File(output, "lib.js")
            val checksumFile = File(output, "lib.js.checksum")
            assertTrue(copied.isFile)
            assertTrue(checksumFile.isFile)
            val firstChecksum = checksumFile.readText()

            // Modify the output to detect whether a second export overwrites it.
            copied.writeText("TAMPERED")

            // Second export: source unchanged, checksum matches -> copy is skipped.
            artifact.accept(FileResourceExporter(output))
            assertEquals("TAMPERED", copied.readText(), "File should not have been overwritten")
            assertEquals(firstChecksum, checksumFile.readText())
        }

    @Test
    fun `checksum re-copies when source changes`() =
        withTempDir { dir ->
            val source = File(dir, "src").also { it.mkdir() }
            val sourceFile = File(source, "lib.js").also { it.writeText("v1") }

            val output = File(dir, "out").also { it.mkdir() }
            val artifact = FileReferenceOutputArtifact(name = "lib.js", file = sourceFile, useChecksumInvalidation = true)

            // First export.
            artifact.accept(FileResourceExporter(output))
            val firstChecksum = File(output, "lib.js.checksum").readText()

            // Modify source.
            sourceFile.writeText("v2")

            // Second export: checksum differs -> file is re-copied.
            artifact.accept(FileResourceExporter(output))
            assertEquals("v2", File(output, "lib.js").readText())

            val secondChecksum = File(output, "lib.js.checksum").readText()
            assertTrue(firstChecksum != secondChecksum)
        }

    @Test
    fun `checksum works for directories`() =
        withTempDir { dir ->
            val source = File(dir, "fonts").also { it.mkdir() }
            File(source, "a.woff2").writeText("font-a")

            val output = File(dir, "out").also { it.mkdir() }
            val artifact = FileReferenceOutputArtifact(name = "fonts", file = source, useChecksumInvalidation = true)

            // First export.
            artifact.accept(FileResourceExporter(output))
            assertTrue(File(output, "fonts/a.woff2").isFile)
            assertTrue(File(output, "fonts.checksum").isFile)
            val firstChecksum = File(output, "fonts.checksum").readText()

            // Add a file to source.
            File(source, "b.woff2").writeText("font-b")

            // Second export: checksum differs -> directory is re-copied.
            artifact.accept(FileResourceExporter(output))
            assertTrue(File(output, "fonts/b.woff2").isFile)

            val secondChecksum = File(output, "fonts.checksum").readText()
            assertTrue(firstChecksum != secondChecksum)
        }

    @Test
    fun `no checksum file when invalidation is disabled`() =
        withTempDir { dir ->
            val source = File(dir, "src").also { it.mkdir() }
            File(source, "lib.js").writeText("hello")

            val output = File(dir, "out").also { it.mkdir() }
            val artifact = FileReferenceOutputArtifact(name = "lib.js", file = File(source, "lib.js"))

            artifact.accept(FileResourceExporter(output))
            assertTrue(File(output, "lib.js").isFile)
            assertFalse(File(output, "lib.js.checksum").exists())
        }
}
