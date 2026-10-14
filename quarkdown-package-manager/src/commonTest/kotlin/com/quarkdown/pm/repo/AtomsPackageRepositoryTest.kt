package com.quarkdown.pm.repo

import com.quarkdown.pm.manifest.ManifestParseException
import com.quarkdown.pm.model.PackageIdentifier
import com.quarkdown.pm.model.PackageReference
import com.quarkdown.pm.model.download
import com.quarkdown.pm.net.FakeRemoteFetcher
import com.quarkdown.pm.net.RemoteFetchException
import com.quarkdown.pm.repo.github.GitHubRepositoryCoordinates
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Tests for [AtomsPackageRepository] against a fake GitHub backend.
 */
class AtomsPackageRepositoryTest {
    private val coordinates = GitHubRepositoryCoordinates("quarkdown-labs", "atoms")
    private val cv = PackageIdentifier(scope = "atoms", name = "cv")

    private fun ref(
        version: String,
        name: String = cv.name,
    ) = PackageReference(cv.copy(name = name), version)

    private val cvTree =
        """
        {
          "sha": "abc",
          "tree": [
            { "path": "1.0.0", "type": "tree" },
            { "path": "1.0.0/atom.toml", "type": "blob", "size": 1 },
            { "path": "1.0.0/main.qd", "type": "blob", "size": 1 },
            { "path": "1.0.0/assets/logo.png", "type": "blob", "size": 1 },
            { "path": "1.0.0/__tests/basic/main.qd", "type": "blob", "size": 1 },
            { "path": "1.0.0/__notes.md", "type": "blob", "size": 1 },
            { "path": "1.0.0/assets/__draft.png", "type": "blob", "size": 1 },
            { "path": "2.0.0/atom.toml", "type": "blob", "size": 1 },
            { "path": "2.0.0/unpublished.qd", "type": "blob", "size": 1 }
          ],
          "truncated": false
        }
        """.trimIndent()

    private fun singleFileTree(path: String) = """{ "sha": "x", "tree": [ { "path": "$path", "type": "blob", "size": 1 } ] }"""

    private val manifestToml =
        """
        [atom]
        name = "cv"
        version = "1.0.0"
        entrypoint = "main.qd"
        description = "CV template"
        """.trimIndent()

    private val png = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0xFF.toByte(), 0xFE.toByte())

    private fun raw(path: String) = coordinates.rawFileUrl(path).toString()

    private fun fetcher() =
        FakeRemoteFetcher(
            mapOf(
                coordinates.subtreeUrl("cv").toString() to cvTree.encodeToByteArray(),
                coordinates.subtreeUrl("broken").toString() to singleFileTree("1.0.0/atom.toml").encodeToByteArray(),
                coordinates.subtreeUrl("nomanifest").toString() to singleFileTree("1.0.0/main.qd").encodeToByteArray(),
                raw("cv/1.0.0/atom.toml") to manifestToml.encodeToByteArray(),
                raw("cv/1.0.0/main.qd") to "# CV".encodeToByteArray(),
                raw("cv/1.0.0/assets/logo.png") to png,
                raw("cv/2.0.0/atom.toml") to manifestToml.replace("1.0.0", "2.0.0").encodeToByteArray(),
                raw("broken/1.0.0/atom.toml") to "[atom\nname =".encodeToByteArray(),
            ),
        )

    private fun repository(fetcher: FakeRemoteFetcher = fetcher()) = AtomsPackageRepository(coordinates, fetcher)

    @Test
    fun `resolves existing package with manifest`() {
        val pkg = assertNotNull(repository().getPackage(ref("1.0.0")))
        assertEquals("cv", pkg.manifest.atom.name)
        assertEquals("CV template", pkg.manifest.atom.description)
    }

    @Test
    fun `lists files relative to the package root`() {
        val pkg = assertNotNull(repository().getPackage(ref("1.0.0")))
        assertEquals(listOf("atom.toml", "main.qd", "assets/logo.png"), pkg.files.map { it.path })
    }

    @Test
    fun `files and directories starting with a double underscore are not part of the package`() {
        val pkg = assertNotNull(repository().getPackage(ref("1.0.0")))
        assertEquals(listOf("atom.toml", "main.qd", "assets/logo.png"), pkg.files.map { it.path })
    }

    @Test
    fun `unknown package is null`() {
        assertNull(repository().getPackage(ref("1.0.0", name = "missing")))
    }

    @Test
    fun `unknown version is null`() {
        assertNull(repository().getPackage(ref("9.9.9")))
    }

    @Test
    fun `version directory without manifest is null`() {
        assertNull(repository().getPackage(ref("1.0.0", name = "nomanifest")))
    }

    @Test
    fun `malformed manifest raises`() {
        assertFailsWith<ManifestParseException> { repository().getPackage(ref("1.0.0", name = "broken")) }
    }

    @Test
    fun `file content is fetched lazily and memoized`() {
        val fetcher = fetcher()
        val pkg = assertNotNull(repository(fetcher).getPackage(ref("1.0.0")))
        assertEquals(
            listOf(coordinates.subtreeUrl("cv").toString(), raw("cv/1.0.0/atom.toml")),
            fetcher.requested.map { it.toString() },
        )
        val main = pkg.files.single { it.path == "main.qd" }
        assertEquals("# CV", main.content().decodeToString())
        main.content()
        assertEquals(1, fetcher.requested.count { it.toString() == raw("cv/1.0.0/main.qd") })
    }

    @Test
    fun `download materializes text and binary files`() {
        val fs = assertNotNull(repository().getPackage(ref("1.0.0"))).download()
        assertEquals("# CV", fs.resolve("main.qd").readText())
        assertEquals(manifestToml, fs.resolve("atom.toml").readText())
        assertEquals(png.toList(), fs.resolve("assets/logo.png").readBytes().toList())
    }

    @Test
    fun `a package listing is fetched once across its lookups`() {
        val fetcher = fetcher()
        val repo = repository(fetcher)
        repo.getPackage(ref("1.0.0"))
        repo.getPackage(ref("2.0.0"))
        repo.versionsOf(cv)
        assertEquals(1, fetcher.requested.count { it == coordinates.subtreeUrl("cv") })
    }

    @Test
    fun `an unknown package is looked up once and never fetched again`() {
        val fetcher = fetcher()
        val repo = repository(fetcher)
        assertNull(repo.getPackage(ref("1.0.0", name = "missing")))
        assertNull(repo.getPackage(ref("2.0.0", name = "missing")))
        assertEquals(1, fetcher.requested.count { it == coordinates.subtreeUrl("missing") })
    }

    @Test
    fun `lists versions of a package`() {
        assertEquals(setOf("1.0.0", "2.0.0"), repository().versionsOf(cv))
        assertEquals(emptySet(), repository().versionsOf(cv.copy(name = "missing")))
    }

    @Test
    fun `unreachable listing propagates fetch failure`() {
        val repo = AtomsPackageRepository(coordinates, { url -> throw RemoteFetchException(url, "connection refused") })
        assertFailsWith<RemoteFetchException> { repo.getPackage(ref("1.0.0")) }
    }

    @Test
    fun `missing file propagates fetch failure on content`() {
        val pkg = assertNotNull(repository().getPackage(ref("2.0.0")))
        val unpublished = pkg.files.single { it.path == "unpublished.qd" }
        assertFailsWith<RemoteFetchException.NotFound> { unpublished.content() }
    }
}
