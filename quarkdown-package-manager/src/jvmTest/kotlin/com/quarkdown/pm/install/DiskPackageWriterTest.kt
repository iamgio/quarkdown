package com.quarkdown.pm.install

import com.quarkdown.core.filesystem.VirtualFileSystem
import com.quarkdown.core.filesystem.toFsEntry
import com.quarkdown.pm.model.AtomManifest
import com.quarkdown.pm.model.PackageFile
import com.quarkdown.pm.model.PackageManifest
import com.quarkdown.pm.model.RepositoryPackage
import com.quarkdown.pm.net.RemoteFetchException
import io.ktor.http.Url
import java.io.File
import java.util.concurrent.CountDownLatch
import kotlin.concurrent.thread
import kotlin.io.path.createTempDirectory
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse

/**
 * Tests for [DiskPackageWriter].
 */
class DiskPackageWriterTest {
    private val project = createTempDirectory("qd-pm").toFile()
    private val target = File(project, "@atoms/cv")
    private val manifest = PackageManifest(AtomManifest("cv", "1.0.0", "main.qd"))
    private val png = byteArrayOf(0x89.toByte(), 0x50, 0xFF.toByte())

    @AfterTest
    fun cleanup() {
        project.deleteRecursively()
    }

    private fun pkg(vararg files: Pair<String, () -> ByteArray>) =
        RepositoryPackage(manifest, files.map { (path, content) -> PackageFile(path, content) })

    @Test
    fun `writes nested text and binary files`() {
        DiskPackageWriter().write(pkg("main.qd" to { "# CV".encodeToByteArray() }, "assets/logo.png" to { png }), target.toFsEntry())
        assertEquals("# CV", File(target, "main.qd").readText())
        assertEquals(png.toList(), File(target, "assets/logo.png").readBytes().toList())
    }

    @Test
    fun `replaces an existing installation entirely`() {
        File(target, "stale.qd").apply {
            parentFile.mkdirs()
            writeText("old")
        }
        DiskPackageWriter().write(pkg("main.qd" to { "new".encodeToByteArray() }), target.toFsEntry())
        assertFalse(File(target, "stale.qd").exists())
        assertEquals("new", File(target, "main.qd").readText())
    }

    @Test
    fun `failed download leaves no package directory and no temporary files`() {
        val failing =
            pkg(
                "main.qd" to { "ok".encodeToByteArray() },
                "assets/logo.png" to { throw RemoteFetchException.NotFound(Url("https://example.com/logo.png")) },
            )
        assertFailsWith<RemoteFetchException> { DiskPackageWriter().write(failing, target.toFsEntry()) }
        assertFalse(target.exists())
        assertEquals(emptyList(), File(project, "@atoms").listFiles().orEmpty().map { it.name })
    }

    @Test
    fun `failed download keeps the previous installation intact`() {
        File(target, "main.qd").apply {
            parentFile.mkdirs()
            writeText("previous")
        }
        val failing = pkg("main.qd" to { throw RemoteFetchException.NotFound(Url("https://example.com/main.qd")) })
        assertFailsWith<RemoteFetchException> { DiskPackageWriter().write(failing, target.toFsEntry()) }
        assertEquals("previous", File(target, "main.qd").readText())
    }

    @Test
    fun `concurrent writes to the same target leave exactly one complete package and no staging leftovers`() {
        val fileCount = 300

        fun packageNamed(tag: String) = pkg(*Array(fileCount) { i -> "$tag/file$i.txt" to { tag.encodeToByteArray() } })
        val start = CountDownLatch(1)
        val errors = mutableListOf<Throwable>()
        val threads =
            listOf("a", "b").map { tag ->
                thread {
                    start.await()
                    try {
                        DiskPackageWriter().write(packageNamed(tag), target.toFsEntry())
                    } catch (e: Throwable) {
                        synchronized(errors) { errors += e }
                    }
                }
            }
        start.countDown()
        threads.forEach { it.join() }

        assertEquals(emptyList(), errors)
        val installed =
            target
                .listFiles()
                .orEmpty()
                .map { it.name }
                .sorted()
        assertEquals(1, installed.size, "expected one package, found $installed")
        assertEquals(fileCount, File(target, installed.single()).listFiles().orEmpty().size)
        assertEquals(listOf("cv"), File(project, "@atoms").listFiles().orEmpty().map { it.name })
    }

    @Test
    fun `non-disk target is rejected`() {
        val virtual = VirtualFileSystem("/p").resolve("/p/@atoms/cv")
        assertFailsWith<PackageWriteException> { DiskPackageWriter().write(pkg(), virtual) }
    }
}
