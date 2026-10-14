package com.quarkdown.pm.resolve

import com.quarkdown.core.filesystem.VirtualFileSystem
import com.quarkdown.core.packages.PackageResolutionException
import com.quarkdown.pm.install.VirtualFileSystemPackageWriter
import com.quarkdown.pm.net.FakeRemoteFetcher
import com.quarkdown.pm.repo.ATOMS_SCOPE
import com.quarkdown.pm.repo.AtomsPackageRepository
import com.quarkdown.pm.repo.PackageRepositories
import com.quarkdown.pm.repo.github.GitHubRepositoryCoordinates
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for [LocalPackageResolver] over an in-memory project and a fake GitHub backend.
 */
class LocalPackageResolverTest {
    private val coordinates = GitHubRepositoryCoordinates("quarkdown-labs", "atoms")
    private val cv1 = "@atoms/cv:1.0.0"
    private val cv2 = "@atoms/cv:2.0.0"

    private val treeJson =
        """
        {
          "sha": "abc",
          "tree": [
            { "path": "1.0.0/atom.toml", "type": "blob" },
            { "path": "1.0.0/main.qd", "type": "blob" },
            { "path": "1.0.0/assets/logo.png", "type": "blob" },
            { "path": "2.0.0/atom.toml", "type": "blob" },
            { "path": "2.0.0/index.qd", "type": "blob" }
          ],
          "truncated": false
        }
        """.trimIndent()

    private fun manifest(
        version: String,
        entrypoint: String,
    ) = """
        [atom]
        name = "cv"
        version = "$version"
        entrypoint = "$entrypoint"
        """.trimIndent()

    private fun raw(path: String) = coordinates.rawFileUrl(path).toString()

    private fun fetcher() =
        FakeRemoteFetcher.ofText(
            mapOf(
                coordinates.subtreeUrl("cv").toString() to treeJson,
                raw("cv/1.0.0/atom.toml") to manifest("1.0.0", "main.qd"),
                raw("cv/1.0.0/main.qd") to "# CV 1",
                raw("cv/1.0.0/assets/logo.png") to "png",
                raw("cv/2.0.0/atom.toml") to manifest("2.0.0", "index.qd"),
                raw("cv/2.0.0/index.qd") to "# CV 2",
            ),
        )

    private val fs = VirtualFileSystem("/project")
    private val root = fs.resolve("/project")

    private fun resolver(fetcher: FakeRemoteFetcher = fetcher()) =
        LocalPackageResolver(
            repositories = PackageRepositories(mapOf(ATOMS_SCOPE to AtomsPackageRepository(coordinates, fetcher))),
            writer = VirtualFileSystemPackageWriter(fs),
        )

    @Test
    fun `locate misses when nothing is installed and downloads nothing`() {
        val fetcher = fetcher()
        assertNull(resolver(fetcher).locate(cv1, root))
        assertEquals(emptyList(), fetcher.requested)
    }

    @Test
    fun `install writes the package and resolves the manifest entry point`() {
        val resolved = resolver().install(cv1, root)
        assertEquals("/project/@atoms/cv", resolved.root.invariantSeparatorsPath)
        assertEquals("/project/@atoms/cv/main.qd", resolved.entrypoint.invariantSeparatorsPath)
        assertEquals("# CV 1", resolved.entrypoint.readText())
        assertTrue(root.resolve("@atoms/cv/assets/logo.png").exists)
        assertTrue(root.resolve("@atoms/cv/atom.toml").exists)
    }

    @Test
    fun `locate hits after install and skips the network`() {
        val fetcher = fetcher()
        val resolver = resolver(fetcher)
        resolver.install(cv1, root)
        val requests = fetcher.requested.size
        val hit = assertNotNull(resolver.locate(cv1, root))
        assertEquals("/project/@atoms/cv/main.qd", hit.entrypoint.invariantSeparatorsPath)
        assertEquals(requests, fetcher.requested.size)
    }

    @Test
    fun `locate misses when the installed version differs`() {
        val resolver = resolver()
        resolver.install(cv1, root)
        assertNull(resolver.locate(cv2, root))
    }

    @Test
    fun `installing another version replaces the directory`() {
        val resolver = resolver()
        resolver.install(cv1, root)
        val resolved = resolver.install(cv2, root)
        assertEquals("/project/@atoms/cv/index.qd", resolved.entrypoint.invariantSeparatorsPath)
        assertFalse(root.resolve("@atoms/cv/main.qd").exists)
        assertFalse(root.resolve("@atoms/cv/assets/logo.png").exists)
        assertNull(resolver.locate(cv1, root))
    }

    @Test
    fun `locate misses after the installation is deleted`() {
        val resolver = resolver()
        resolver.install(cv1, root)
        assertNotNull(resolver.locate(cv1, root))
        fs.delete("/project/@atoms/cv")
        assertNull(resolver.locate(cv1, root))
    }

    @Test
    fun `malformed reference is a resolution error naming it`() {
        val exception = assertFailsWith<PackageResolutionException> { resolver().locate("@atoms/cv", root) }
        assertContains(exception.message.orEmpty(), "@atoms/cv")
    }

    @Test
    fun `unknown scope is a resolution error`() {
        val exception = assertFailsWith<PackageResolutionException> { resolver().install("@foo/cv:1.0.0", root) }
        assertContains(exception.message.orEmpty(), "foo")
    }

    @Test
    fun `unknown version is a resolution error`() {
        val exception = assertFailsWith<PackageResolutionException> { resolver().install("@atoms/cv:9.9.9", root) }
        assertContains(exception.message.orEmpty(), "@atoms/cv:9.9.9")
    }

    @Test
    fun `network failure is a resolution error`() {
        val exception = assertFailsWith<PackageResolutionException> { resolver(FakeRemoteFetcher(emptyMap())).install(cv1, root) }
        assertContains(exception.message.orEmpty(), cv1)
    }

    @Test
    fun `non-json tree response is a resolution error carrying its cause`() {
        val fetcher = FakeRemoteFetcher.ofText(mapOf(coordinates.subtreeUrl("cv").toString() to "<html>rate limited</html>"))
        val exception = assertFailsWith<PackageResolutionException> { resolver(fetcher).install(cv1, root) }
        assertContains(exception.message.orEmpty(), cv1)
        assertNotNull(exception.cause)
    }

    @Test
    fun `tree entry escaping the package directory is a resolution error`() {
        val fetcher =
            FakeRemoteFetcher.ofText(
                mapOf(
                    coordinates.subtreeUrl("cv").toString() to
                        """
                        { "sha": "x", "tree": [
                          { "path": "1.0.0/atom.toml", "type": "blob" },
                          { "path": "1.0.0/a\\..\\x.qd", "type": "blob" }
                        ] }
                        """.trimIndent(),
                    raw("cv/1.0.0/atom.toml") to manifest("1.0.0", "main.qd"),
                ),
            )
        val exception = assertFailsWith<PackageResolutionException> { resolver(fetcher).install(cv1, root) }
        assertContains(exception.message.orEmpty(), cv1)
        assertFalse(root.resolve("@atoms/cv").exists)
    }

    @Test
    fun `locate reports a deleted entry point`() {
        val resolver = resolver()
        resolver.install(cv1, root)
        assertNotNull(resolver.locate(cv1, root))
        fs.delete("/project/@atoms/cv/main.qd")
        val exception = assertFailsWith<PackageResolutionException> { resolver.locate(cv1, root) }
        assertContains(exception.message.orEmpty(), "main.qd")
    }

    @Test
    fun `corrupted installed manifest is reported, not overwritten`() {
        fs.write("/project/@atoms/cv/atom.toml", "[atom\nname =")
        fs.write("/project/@atoms/cv/main.qd", "edited")
        val exception = assertFailsWith<PackageResolutionException> { resolver().locate(cv1, root) }
        assertContains(exception.message.orEmpty(), "/project/@atoms/cv")
        assertEquals("edited", root.resolve("@atoms/cv/main.qd").readText())
    }

    @Test
    fun `installed manifest with missing entry point is reported`() {
        fs.write("/project/@atoms/cv/atom.toml", manifest("1.0.0", "main.qd"))
        val exception = assertFailsWith<PackageResolutionException> { resolver().locate(cv1, root) }
        assertContains(exception.message.orEmpty(), "main.qd")
    }
}
