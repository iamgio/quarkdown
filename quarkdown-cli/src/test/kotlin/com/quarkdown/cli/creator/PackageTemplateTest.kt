package com.quarkdown.cli.creator

import com.quarkdown.cli.creator.content.PackageTemplate
import com.quarkdown.core.document.DocumentType
import com.quarkdown.core.packages.PackageResolutionException
import com.quarkdown.pm.model.AtomManifest
import com.quarkdown.pm.model.PackageFile
import com.quarkdown.pm.model.PackageManifest
import com.quarkdown.pm.model.RepositoryPackage
import com.quarkdown.pm.model.TemplateManifest
import com.quarkdown.pm.net.RemoteFetchException
import io.ktor.http.Url
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

/**
 * Tests for [PackageTemplate.resolve].
 */
class PackageTemplateTest {
    private val cvTemplate =
        TemplateManifest(
            doctype = "paged",
            content = "template.qd",
            additionalFiles = listOf("sections/experience.qd", "references.bib"),
        )

    private val cvFiles =
        mapOf(
            "main.qd" to "# CV library",
            "template.qd" to "# My CV",
            "sections/experience.qd" to "# Experience",
            "references.bib" to "@book{}",
        )

    private val repositories =
        FakePackageRepository(
            mapOf(
                "@atoms/cv:1.0.0" to templatePackage("cv", template = cvTemplate, files = cvFiles),
                "@atoms/untyped:1.0.0" to templatePackage("untyped", template = TemplateManifest(content = "template.qd"), files = cvFiles),
                "@atoms/plain:1.0.0" to templatePackage("plain", files = cvFiles),
                "@atoms/nocontent:1.0.0" to templatePackage("nocontent", template = cvTemplate, files = cvFiles - "template.qd"),
                "@atoms/nofile:1.0.0" to templatePackage("nofile", template = cvTemplate, files = cvFiles - "references.bib"),
                "@atoms/badtype:1.0.0" to templatePackage("badtype", template = cvTemplate.copy(doctype = "poster"), files = cvFiles),
            ),
        ).asAtoms()

    private fun resolve(reference: String) = PackageTemplate.resolve(repositories, reference)

    private fun assertRefused(
        reference: String,
        detail: String,
    ) {
        val message = assertFailsWith<PackageResolutionException> { resolve(reference) }.message!!
        assertContains(message, reference)
        assertContains(message, detail)
    }

    @Test
    fun `resolves a template with doctype, content and additional files`() {
        val template = resolve("@atoms/cv:1.0.0")
        assertEquals("@atoms/cv:1.0.0", template.reference)
        assertEquals(DocumentType.PAGED, template.doctype)
        assertEquals("# My CV", template.content)
        assertEquals("# Experience", template.additionalFiles.resolve("sections/experience.qd").readText())
        assertEquals("@book{}", template.additionalFiles.resolve("references.bib").readText())
        assertEquals(
            setOf("sections", "references.bib"),
            template.additionalFiles
                .children()
                .map { it.name }
                .toSet(),
        )
    }

    @Test
    fun `a failed download is a resolution failure naming the reference`() {
        val unreachable =
            RepositoryPackage(
                PackageManifest(
                    AtomManifest(name = "offline", version = "1.0.0", entrypoint = "main.qd"),
                    TemplateManifest(content = "template.qd", additionalFiles = listOf("assets/photo.png")),
                ),
                listOf(
                    PackageFile("template.qd") { "# T".encodeToByteArray() },
                    PackageFile("assets/photo.png") { throw RemoteFetchException.NotFound(Url("https://example.com/photo.png")) },
                ),
            )
        val repositories = FakePackageRepository(mapOf("@atoms/offline:1.0.0" to unreachable)).asAtoms()
        val e = assertFailsWith<PackageResolutionException> { PackageTemplate.resolve(repositories, "@atoms/offline:1.0.0") }
        assertContains(e.message!!, "@atoms/offline:1.0.0")
        assertContains(e.message!!, "https://example.com/photo.png")
    }

    @Test
    fun `template without doctype leaves it null`() {
        assertNull(resolve("@atoms/untyped:1.0.0").doctype)
    }

    @Test
    fun `package without template table is refused`() {
        assertRefused("@atoms/plain:1.0.0", "exists but is not a template")
    }

    @Test
    fun `missing content file is refused`() {
        assertRefused("@atoms/nocontent:1.0.0", "declares 'template.qd' as a template file, but the package has no such file")
    }

    @Test
    fun `missing additional file is refused`() {
        assertRefused("@atoms/nofile:1.0.0", "declares 'references.bib' as a template file, but the package has no such file")
    }

    @Test
    fun `unknown doctype is refused`() {
        assertRefused("@atoms/badtype:1.0.0", "declares unknown document type 'poster'")
    }

    @Test
    fun `missing package is refused`() {
        assertRefused("@atoms/missing:1.0.0", "not found in repository 'atoms'")
    }

    @Test
    fun `unknown scope is refused`() {
        assertRefused("@nope/cv:1.0.0", "no package repository is registered for scope 'nope'")
    }

    @Test
    fun `malformed reference is refused`() {
        assertRefused("@atoms/cv", "Malformed package reference")
    }
}
