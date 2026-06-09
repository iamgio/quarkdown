package com.quarkdown.core

import com.quarkdown.core.pipeline.output.ArtifactType
import com.quarkdown.core.pipeline.output.BinaryOutputArtifact
import com.quarkdown.core.pipeline.output.FileReferenceOutputArtifact
import com.quarkdown.core.pipeline.output.OutputResourceGroup
import com.quarkdown.core.pipeline.output.TextOutputArtifact
import com.quarkdown.core.pipeline.output.visitor.FileResourceExporter
import org.junit.Assume.assumeTrue
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.attribute.BasicFileAttributes
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
    fun `checksum re-copies when target deleted but checksum file survives`() =
        withTempDir { dir ->
            val source = File(dir, "src").also { it.mkdir() }
            val sourceFile = File(source, "lib.js").also { it.writeText("content") }

            val output = File(dir, "out").also { it.mkdir() }
            val artifact = FileReferenceOutputArtifact(name = "lib.js", file = sourceFile, useChecksumInvalidation = true)

            // First export.
            artifact.accept(FileResourceExporter(output))
            assertTrue(File(output, "lib.js").isFile)

            // Delete the target but leave the checksum file.
            File(output, "lib.js").delete()
            assertTrue(File(output, "lib.js.checksum").isFile)

            // Second export: checksum matches but target is missing -> must re-copy.
            artifact.accept(FileResourceExporter(output))
            assertTrue(File(output, "lib.js").isFile)
            assertEquals("content", File(output, "lib.js").readText())
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

    // Symlink references

    /**
     * Probes whether the underlying filesystem can create symbolic links in [dir].
     * Skips the calling test (via JUnit assumption) if it cannot - notably the default
     * configuration on Windows, where `SeCreateSymbolicLinkPrivilege` is not granted to
     * standard users.
     */
    private fun assumeSymlinksWork(dir: File) {
        val probeSource = File(dir, "_probe_src").also { it.writeText("probe") }
        val probeLink = File(dir, "_probe_link")
        val supported =
            try {
                Files.createSymbolicLink(probeLink.toPath(), probeSource.toPath())
                true
            } catch (_: IOException) {
                false
            } catch (_: UnsupportedOperationException) {
                false
            }
        Files.deleteIfExists(probeLink.toPath())
        probeSource.delete()
        assumeTrue("Symbolic links are not supported in this environment", supported)
    }

    @Test
    fun `symlink reference creates a symbolic link to a file`() =
        withTempDir { dir ->
            assumeSymlinksWork(dir)

            val source = File(dir, "src").also { it.mkdir() }
            val sourceFile = File(source, "lib.js").also { it.writeText("var v = 1;") }

            val output = File(dir, "out").also { it.mkdir() }
            val artifact = FileReferenceOutputArtifact(name = "lib.js", file = sourceFile, symlink = true)
            val result = artifact.accept(FileResourceExporter(output))

            assertTrue(Files.isSymbolicLink(result.toPath()))
            assertEquals("var v = 1;", result.readText())
        }

    @Test
    fun `symlink reference creates a symbolic link to a directory`() =
        withTempDir { dir ->
            assumeSymlinksWork(dir)

            val source = File(dir, "lib").also { it.mkdir() }
            File(source, "a.js").writeText("var a;")
            File(source, "sub").mkdir()
            File(source, "sub/b.js").writeText("var b;")

            val output = File(dir, "out").also { it.mkdir() }
            val artifact = FileReferenceOutputArtifact(name = "lib", file = source, symlink = true)
            val result = artifact.accept(FileResourceExporter(output))

            assertTrue(Files.isSymbolicLink(result.toPath()))
            assertTrue(result.isDirectory)
            assertEquals("var a;", File(result, "a.js").readText())
            assertEquals("var b;", File(result, "sub/b.js").readText())
        }

    @Test
    fun `symlink reflects changes to the source without re-export`() =
        withTempDir { dir ->
            assumeSymlinksWork(dir)

            val source = File(dir, "src").also { it.mkdir() }
            val sourceFile = File(source, "lib.js").also { it.writeText("v1") }

            val output = File(dir, "out").also { it.mkdir() }
            val artifact = FileReferenceOutputArtifact(name = "lib.js", file = sourceFile, symlink = true)
            val result = artifact.accept(FileResourceExporter(output))

            assertEquals("v1", result.readText())

            // Mutating the source must be visible through the link, since it's not a copy.
            sourceFile.writeText("v2")
            assertEquals("v2", result.readText())
        }

    @Test
    fun `symlink replaces an existing regular file at target`() =
        withTempDir { dir ->
            assumeSymlinksWork(dir)

            val source = File(dir, "src").also { it.mkdir() }
            val sourceFile = File(source, "lib.js").also { it.writeText("source") }

            val output = File(dir, "out").also { it.mkdir() }
            File(output, "lib.js").writeText("stale copy")

            val artifact = FileReferenceOutputArtifact(name = "lib.js", file = sourceFile, symlink = true)
            val result = artifact.accept(FileResourceExporter(output))

            assertTrue(Files.isSymbolicLink(result.toPath()))
            assertEquals("source", result.readText())
        }

    @Test
    fun `symlink replaces a stale broken symlink at target`() =
        withTempDir { dir ->
            assumeSymlinksWork(dir)

            val output = File(dir, "out").also { it.mkdir() }

            // Leftover from a previous export, pointing at a now-missing source.
            val ghost = File(dir, "ghost-source")
            Files.createSymbolicLink(File(output, "lib.js").toPath(), ghost.toPath())
            assertTrue(Files.isSymbolicLink(File(output, "lib.js").toPath()))
            assertFalse(File(output, "lib.js").exists(), "Broken link should not resolve")

            val source = File(dir, "src").also { it.mkdir() }
            val sourceFile = File(source, "lib.js").also { it.writeText("fresh") }
            val artifact = FileReferenceOutputArtifact(name = "lib.js", file = sourceFile, symlink = true)
            val result = artifact.accept(FileResourceExporter(output))

            assertTrue(Files.isSymbolicLink(result.toPath()))
            assertEquals("fresh", result.readText())
        }

    @Test
    fun `symlink replaces a previously copied non-empty directory at target`() =
        withTempDir { dir ->
            assumeSymlinksWork(dir)

            val output = File(dir, "out").also { it.mkdir() }

            // Simulate a prior non-preview run: a real directory copy lives at the target,
            // and it must be torn down (recursively) before the symlink can be created.
            val stale = File(output, "lib").also { it.mkdir() }
            File(stale, "a.js").writeText("old-a")
            File(stale, "sub").mkdir()
            File(stale, "sub/b.js").writeText("old-b")

            val source = File(dir, "src").also { it.mkdir() }
            File(source, "a.js").writeText("new-a")

            val artifact = FileReferenceOutputArtifact(name = "lib", file = source, symlink = true)
            val result = artifact.accept(FileResourceExporter(output))

            assertTrue(Files.isSymbolicLink(result.toPath()))
            assertEquals("new-a", File(result, "a.js").readText())
            // The stale `sub/` from the previous copy must not leak through the link.
            assertFalse(File(result, "sub").exists())
        }

    @Test
    fun `symlink replaces a live symlink to a directory without touching its source`() =
        withTempDir { dir ->
            assumeSymlinksWork(dir)

            // The original source directory (e.g. lib/<library> in the install layout) MUST be
            // preserved across re-runs. This guards against deleteRecursively following the link.
            val protectedSource = File(dir, "install/lib").also { it.mkdirs() }
            File(protectedSource, "do-not-delete.js").writeText("install layout contents")
            File(protectedSource, "sub").mkdir()
            File(protectedSource, "sub/nested.js").writeText("nested install contents")

            val output = File(dir, "out").also { it.mkdir() }
            val link = File(output, "lib")
            Files.createSymbolicLink(link.toPath(), protectedSource.toPath())
            assertTrue(Files.isSymbolicLink(link.toPath()))

            // Re-run the visitor with the same symlink artifact, simulating a second preview build.
            val artifact = FileReferenceOutputArtifact(name = "lib", file = protectedSource, symlink = true)
            val result = artifact.accept(FileResourceExporter(output))

            assertTrue(Files.isSymbolicLink(result.toPath()))
            // The layout files must still be intact.
            assertTrue(File(protectedSource, "do-not-delete.js").isFile, "Symlink source was destroyed")
            assertTrue(File(protectedSource, "sub/nested.js").isFile, "Nested symlink source was destroyed")
        }

    @Test
    fun `deletion does not follow symlinks nested inside a stale directory at target`() =
        withTempDir { dir ->
            assumeSymlinksWork(dir)

            // Something precious living *outside* the export tree that an internal symlink reaches into.
            val protectedSource = File(dir, "outside").also { it.mkdir() }
            File(protectedSource, "do-not-delete.js").writeText("must survive")

            val output = File(dir, "out").also { it.mkdir() }

            // A stale plain directory left behind by a previous run, with an internal symlink
            // pointing into the protected source. If the recursive delete followed the link,
            // `do-not-delete.js` would be wiped.
            val stale = File(output, "lib").also { it.mkdir() }
            File(stale, "regular.js").writeText("stale regular file")
            Files.createSymbolicLink(File(stale, "linked").toPath(), protectedSource.toPath())

            val source = File(dir, "src").also { it.mkdir() }
            File(source, "new.js").writeText("new content")

            val artifact = FileReferenceOutputArtifact(name = "lib", file = source, symlink = true)
            artifact.accept(FileResourceExporter(output))

            // The protected source must be untouched.
            assertTrue(File(protectedSource, "do-not-delete.js").isFile, "Nested symlink was followed during deletion")
        }

    @Test
    fun `symlink reuses existing link when it already points to the source`() =
        withTempDir { dir ->
            assumeSymlinksWork(dir)

            val source = File(dir, "src").also { it.mkdir() }
            val sourceFile = File(source, "lib.js").also { it.writeText("v1") }

            val output = File(dir, "out").also { it.mkdir() }
            val artifact = FileReferenceOutputArtifact(name = "lib.js", file = sourceFile, symlink = true)

            // First export creates the link.
            val result = artifact.accept(FileResourceExporter(output))
            assertTrue(Files.isSymbolicLink(result.toPath()))

            // Capture the inode (file key) of the link entry itself, NOT its target.
            val keyBefore =
                Files
                    .readAttributes(result.toPath(), BasicFileAttributes::class.java, LinkOption.NOFOLLOW_LINKS)
                    .fileKey()
            // Some filesystems (notably on Windows) don't expose a stable file key.
            if (keyBefore == null) return@withTempDir

            // Second export: same source, same target. The visitor must not delete+recreate
            // the link entry, so the file key must be identical.
            artifact.accept(FileResourceExporter(output))
            val keyAfter =
                Files
                    .readAttributes(result.toPath(), BasicFileAttributes::class.java, LinkOption.NOFOLLOW_LINKS)
                    .fileKey()
            assertEquals(keyBefore, keyAfter, "Symlink entry was recreated despite already pointing at the source")
        }

    @Test
    fun `symlink recreates link when pointing to a different source`() =
        withTempDir { dir ->
            assumeSymlinksWork(dir)

            val sourceA = File(dir, "srcA").also { it.mkdir() }
            val fileA = File(sourceA, "lib.js").also { it.writeText("from A") }
            val sourceB = File(dir, "srcB").also { it.mkdir() }
            val fileB = File(sourceB, "lib.js").also { it.writeText("from B") }

            val output = File(dir, "out").also { it.mkdir() }

            // First export points at A.
            FileReferenceOutputArtifact(name = "lib.js", file = fileA, symlink = true)
                .accept(FileResourceExporter(output))
            val result = File(output, "lib.js")
            assertEquals("from A", result.readText())

            // Second export points at B: the prior link must be replaced, not reused.
            FileReferenceOutputArtifact(name = "lib.js", file = fileB, symlink = true)
                .accept(FileResourceExporter(output))
            assertTrue(Files.isSymbolicLink(result.toPath()))
            assertEquals("from B", result.readText())
        }

    @Test
    fun `symlink takes precedence over checksum invalidation`() =
        withTempDir { dir ->
            assumeSymlinksWork(dir)

            val source = File(dir, "src").also { it.mkdir() }
            val sourceFile = File(source, "lib.js").also { it.writeText("hello") }

            val output = File(dir, "out").also { it.mkdir() }
            val artifact =
                FileReferenceOutputArtifact(
                    name = "lib.js",
                    file = sourceFile,
                    useChecksumInvalidation = true,
                    symlink = true,
                )
            val result = artifact.accept(FileResourceExporter(output))

            assertTrue(Files.isSymbolicLink(result.toPath()))
            // The checksum sidecar belongs to the copy path; it must not be written when symlinking.
            assertFalse(File(output, "lib.js.checksum").exists())
        }
}
