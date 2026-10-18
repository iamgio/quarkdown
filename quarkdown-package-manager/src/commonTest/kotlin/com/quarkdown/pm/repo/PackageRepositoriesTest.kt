package com.quarkdown.pm.repo

import com.quarkdown.core.packages.PackageResolutionException
import com.quarkdown.pm.model.AtomManifest
import com.quarkdown.pm.model.PackageFile
import com.quarkdown.pm.model.PackageManifest
import com.quarkdown.pm.model.PackageReference
import com.quarkdown.pm.model.PackageRepository
import com.quarkdown.pm.model.RepositoryPackage
import com.quarkdown.pm.net.RemoteFetchException
import io.ktor.http.Url
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNull

/**
 * Tests for [PackageRepositories].
 */
class PackageRepositoriesTest {
    private val cv = PackageReference.parse("@atoms/cv:1.0.0")

    private val cvPackage =
        RepositoryPackage(
            PackageManifest(AtomManifest(name = "cv", version = "1.0.0", entrypoint = "main.qd")),
            listOf(PackageFile("main.qd") { "# CV".encodeToByteArray() }),
        )

    private val repository =
        object : PackageRepository {
            override fun getPackage(reference: PackageReference): RepositoryPackage? = cvPackage.takeIf { reference == cv }

            override fun listPackages(): Set<PackageManifest> = setOf(cvPackage.manifest)
        }

    private val unreachable =
        object : PackageRepository {
            override fun getPackage(reference: PackageReference): RepositoryPackage =
                throw RemoteFetchException.NotFound(Url("https://example.com/cv"))

            override fun listPackages(): Set<PackageManifest> = throw RemoteFetchException.NotFound(Url("https://example.com/cv"))
        }

    private fun repositories(repository: PackageRepository = this.repository) = PackageRepositories(mapOf(ATOMS_SCOPE to repository))

    @Test
    fun `default registry maps the atoms scope to the atoms repository`() {
        assertIs<AtomsPackageRepository>(PackageRepositories.default().forScope(ATOMS_SCOPE))
    }

    @Test
    fun `unknown scope is null`() {
        assertNull(PackageRepositories.default().forScope("nope"))
    }

    @Test
    fun `fetch returns the package of the reference's scope`() {
        assertEquals(cvPackage, repositories().fetch(cv))
        assertEquals(cvPackage, repositories().fetch("@atoms/cv:1.0.0"))
    }

    @Test
    fun `fetch of an unknown scope names the scope`() {
        val e = assertFailsWith<PackageResolutionException> { repositories().fetch(PackageReference.parse("@nope/cv:1.0.0")) }
        assertContains(e.message!!, "@nope/cv:1.0.0")
        assertContains(e.message!!, "no package repository is registered for scope 'nope'")
    }

    @Test
    fun `fetch of a missing package names the repository`() {
        val e = assertFailsWith<PackageResolutionException> { repositories().fetch(PackageReference.parse("@atoms/missing:1.0.0")) }
        assertContains(e.message!!, "@atoms/missing:1.0.0")
        assertContains(e.message!!, "not found in repository 'atoms'")
    }

    @Test
    fun `fetch by string translates a malformed reference`() {
        val e = assertFailsWith<PackageResolutionException> { repositories().fetch("@atoms/cv") }
        assertContains(e.message!!, "@atoms/cv")
        assertIs<IllegalArgumentException>(e.cause)
    }

    @Test
    fun `fetch by string translates a network failure`() {
        val e = assertFailsWith<PackageResolutionException> { repositories(unreachable).fetch("@atoms/cv:1.0.0") }
        assertContains(e.message!!, "@atoms/cv:1.0.0")
        assertIs<RemoteFetchException>(e.cause)
    }
}
