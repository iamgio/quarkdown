package com.quarkdown.pm.resolve

import com.quarkdown.core.filesystem.FsEntry
import com.quarkdown.core.filesystem.toFsEntry
import com.quarkdown.core.packages.PackageResolutionException
import org.junit.jupiter.api.Assumptions.assumeTrue
import java.io.File
import java.net.HttpURLConnection
import java.net.URI
import kotlin.io.path.createTempDirectory
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

private const val ATOMS_RAW_HOST = "https://raw.githubusercontent.com"
private const val KANBAN = "@atoms/kanban:1.0.0"

/**
 * Tests for [LocalPackageResolver] against the `@atoms` scope.
 */
class LocalPackageResolverLiveTest {
    private val project = createTempDirectory("qd-pm-live").toFile()
    private val installed = File(project, "@atoms/kanban")
    private val resolver = LocalPackageResolver.disk()

    @BeforeTest
    fun assumeAtomsRepositoryReachable() {
        val reachable =
            runCatching {
                val connection = URI(ATOMS_RAW_HOST).toURL().openConnection() as HttpURLConnection
                connection.connectTimeout = 3_000
                connection.readTimeout = 3_000
                connection.requestMethod = "HEAD"
                connection.responseCode in 200..499
            }.getOrDefault(false)
        assumeTrue(reachable, "The atoms repository is not reachable")
    }

    @AfterTest
    fun cleanup() {
        project.deleteRecursively()
    }

    @Test
    fun `installs the package into the project and locates it afterwards`() {
        assertNull(resolver.locate(KANBAN, project.toFsEntry()))

        val resolved = resolver.install(KANBAN, project.toFsEntry())
        assertEquals(installed, resolved.root.toFile())
        assertEquals(File(installed, "main.qd"), resolved.entrypoint.toFile())
        assertEquals(
            setOf("README.md", "atom.toml", "main.qd"),
            installed
                .listFiles()
                .orEmpty()
                .map { it.name }
                .toSet(),
        )
        assertTrue(
            installed.parentFile
                .listFiles()
                .orEmpty()
                .none { it.name.startsWith(".") },
        )
        assertContains(File(installed, "atom.toml").readText(), "name = \"kanban\"")

        val manifestModifiedAt = File(installed, "atom.toml").lastModified()
        val located = assertNotNull(resolver.locate(KANBAN, project.toFsEntry()))
        assertEquals(resolved.entrypoint.fullPath, located.entrypoint.fullPath)
        assertEquals(manifestModifiedAt, File(installed, "atom.toml").lastModified())
    }

    @Test
    fun `unknown version fails and leaves the installed package untouched`() {
        resolver.install(KANBAN, project.toFsEntry())

        val exception = assertFailsWith<PackageResolutionException> { resolver.install("@atoms/kanban:9.9.9", project.toFsEntry()) }
        assertContains(exception.message.orEmpty(), "@atoms/kanban:9.9.9")
        assertTrue(File(installed, "main.qd").exists())
        assertNotNull(resolver.locate(KANBAN, project.toFsEntry()))
    }

    private fun FsEntry.toFile(): File = File(fullPath)
}
