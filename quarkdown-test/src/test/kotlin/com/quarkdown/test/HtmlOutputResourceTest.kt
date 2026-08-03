package com.quarkdown.test

import com.quarkdown.core.media.storage.MEDIA_SUBDIRECTORY_NAME
import com.quarkdown.core.pipeline.output.ArtifactType
import com.quarkdown.core.pipeline.output.FileReferenceOutputArtifact
import com.quarkdown.core.pipeline.output.OutputResource
import com.quarkdown.core.pipeline.output.OutputResourceGroup
import com.quarkdown.core.pipeline.output.TextOutputArtifact
import com.quarkdown.test.util.DATA_FOLDER
import com.quarkdown.test.util.execute
import com.quarkdown.test.util.getSubResources
import com.quarkdown.test.util.getSubdocumentGroup
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for generation of HTML output resources.
 */
class HtmlOutputResourceTest {
    /** Working directory that contains a `public/` subdirectory with test static assets. */
    private val staticAssetsDir = File(DATA_FOLDER, "static-assets")

    @Test
    fun `regular output with index, theme and script dirs`() {
        execute(
            source = "",
            outputResourceHook = { group ->
                val resources = getSubResources(group).map { it.name }
                assertContains(resources, "index")
                assertContains(resources, "theme")
                assertContains(resources, "script")
            },
        ) {}
    }

    @Test
    fun `with media`() {
        execute(
            source = "![](img/icon.png)",
            enableMediaStorage = true,
            outputResourceHook = { group ->
                val resources = getSubResources(group).map { it.name }
                assertContains(resources, MEDIA_SUBDIRECTORY_NAME)
            },
        ) {}
    }

    // Static assets (public/ directory)

    /**
     * Finds the static-assets [FileReferenceOutputArtifact] (name `"."`) among the root resources,
     * or `null` if absent.
     */
    private fun findStaticAssetsArtifact(group: OutputResource?): FileReferenceOutputArtifact? =
        getSubResources(group)
            .filterIsInstance<FileReferenceOutputArtifact>()
            .firstOrNull { it.name == "." }

    @Test
    fun `static assets from public directory are included`() {
        execute(
            source = "",
            workingDirectory = staticAssetsDir,
            outputResourceHook = { group ->
                val artifact = findStaticAssetsArtifact(group)
                assertNotNull(artifact, "Expected a static-assets artifact (name '.') in root resources")
                assertTrue(artifact.file.isDirectory)
                assertTrue(artifact.file.resolve("robots.txt").isFile)
                assertTrue(artifact.file.resolve("CNAME").isFile)
            },
        ) {}
    }

    @Test
    fun `static assets include subdirectories`() {
        execute(
            source = "",
            workingDirectory = staticAssetsDir,
            outputResourceHook = { group ->
                val artifact = findStaticAssetsArtifact(group)
                assertNotNull(artifact)
                assertTrue(artifact.file.resolve("assets/icons/favicon.svg").isFile)
            },
        ) {}
    }

    @Test
    fun `no static assets when public directory is absent`() {
        execute(
            source = "",
            outputResourceHook = { group ->
                assertNull(findStaticAssetsArtifact(group))
            },
        ) {}
    }

    @Test
    fun `subdocuments do not include static assets`() {
        execute(
            """
            .doctype {docs}

            [Page](subdoc/page.qd)
            """.trimIndent(),
            workingDirectory = staticAssetsDir,
            outputResourceHook = { group ->
                // Root should have static assets.
                assertNotNull(findStaticAssetsArtifact(group))

                // Subdocument should not.
                val subdocGroup = getSubdocumentGroup(group, "page")
                val subdocStaticAssets =
                    subdocGroup.resources
                        .filterIsInstance<FileReferenceOutputArtifact>()
                        .firstOrNull { it.name == "." }
                assertNull(subdocStaticAssets)
            },
        ) {}
    }

    // Sitemap

    private fun findSitemap(group: OutputResource?): TextOutputArtifact? =
        getSubResources(group)
            .filterIsInstance<TextOutputArtifact>()
            .firstOrNull { it.name == "sitemap.xml" && it.type == ArtifactType.AUTO }

    @Test
    fun `no sitemap when there are no subdocuments`() {
        execute(
            source = ".htmloptions baseurl:{https://example.com}",
            outputResourceHook = { group ->
                assertNull(findSitemap(group))
            },
        ) {}
    }

    @Test
    fun `no sitemap without base url`() {
        execute(
            """
            .doctype {docs}

            [1](subdoc/simple-1.qd)
            """.trimIndent(),
            outputResourceHook = { group ->
                assertNull(findSitemap(group))
            },
        ) {}
    }

    @Test
    fun `sitemap lists root and subdocuments with absolute urls`() {
        execute(
            """
            .htmloptions baseurl:{https://example.com}
            .doctype {docs}

            [1](subdoc/simple-1.qd)
            [2](subdoc/simple-2.qd)
            """.trimIndent(),
            outputResourceHook = { group ->
                val sitemap = findSitemap(group)
                assertNotNull(sitemap)
                val content = sitemap.content.toString()
                assertContains(content, "<loc>https://example.com</loc>")
                assertContains(content, "<loc>https://example.com/simple-1</loc>")
                assertContains(content, "<loc>https://example.com/simple-2</loc>")
            },
        ) {}
    }

    @Test
    fun `sitemap does not duplicate root as index subdocument`() {
        execute(
            """
            .htmloptions baseurl:{https://example.com}
            .doctype {docs}

            [1](subdoc/simple-1.qd)
            """.trimIndent(),
            outputResourceHook = { group ->
                val sitemap = findSitemap(group)
                assertNotNull(sitemap)
                val content = sitemap.content.toString()
                // The base URL should appear exactly once (the root entry).
                val occurrences = Regex("<loc>https://example\\.com</loc>").findAll(content).count()
                assertEquals(1, occurrences)
                assertFalse("<loc>https://example.com/index</loc>" in content)
            },
        ) {}
    }

    // llms.txt

    private fun findLlmsTxt(group: OutputResource?): TextOutputArtifact? =
        getSubResources(group)
            .filterIsInstance<TextOutputArtifact>()
            .firstOrNull { it.name == "llms.txt" && it.type == ArtifactType.AUTO }

    @Test
    fun `no llms_txt when llmstxt not called`() {
        execute(
            """
            .htmloptions baseurl:{https://example.com}
            .doctype {docs}

            [1](subdoc/simple-1.qd)
            """.trimIndent(),
            outputResourceHook = { group ->
                assertNull(findLlmsTxt(group))
            },
        ) {}
    }

    @Test
    fun `no llms_txt when there are no subdocuments`() {
        execute(
            """
            .htmloptions baseurl:{https://example.com}

            .llmstxt markdownavailable:{no}
                A test site.
            """.trimIndent(),
            outputResourceHook = { group ->
                assertNull(findLlmsTxt(group))
            },
        ) {}
    }

    @Test
    fun `llms_txt lists root and subdocuments as html links when markdown mirror not available`() {
        execute(
            """
            .htmloptions baseurl:{https://example.com}
            .doctype {docs}
            .docname {My site}

            .llmstxt markdownavailable:{no}
                A short summary.

            [1](subdoc/simple-1.qd)
            [2](subdoc/simple-2.qd)
            """.trimIndent(),
            outputResourceHook = { group ->
                val llmsTxt = findLlmsTxt(group)
                assertNotNull(llmsTxt)
                val content = llmsTxt.content.toString()
                assertContains(content, "# My site")
                assertContains(content, "> A short summary.")
                assertContains(content, "## Docs")
                // HTML root is served at the bare base URL, not at `<baseUrl>/index`.
                assertContains(content, "[My site](https://example.com)")
                assertFalse("[My site](https://example.com/index)" in content)
                assertContains(content, "(https://example.com/simple-1)")
                assertContains(content, "(https://example.com/simple-2)")
                // No .md suffix since markdownavailable=false.
                assertFalse(".md)" in content)
            },
        ) {}
    }

    @Test
    fun `llms_txt links point at markdown files when markdown mirror available`() {
        execute(
            """
            .htmloptions baseurl:{https://example.com}
            .doctype {docs}
            .docname {My site}

            .llmstxt markdownavailable:{yes}
                A short summary.

            [1](subdoc/simple-1.qd)
            [2](subdoc/simple-2.qd)
            """.trimIndent(),
            outputResourceHook = { group ->
                val llmsTxt = findLlmsTxt(group)
                assertNotNull(llmsTxt)
                val content = llmsTxt.content.toString()
                assertContains(content, "[My site](https://example.com/index.md)")
                assertContains(content, "(https://example.com/simple-1.md)")
                assertContains(content, "(https://example.com/simple-2.md)")
            },
        ) {}
    }

    @Test
    fun `llms_txt still emits root entry when docname is absent`() {
        execute(
            """
            .htmloptions baseurl:{https://example.com}
            .doctype {docs}

            .llmstxt markdownavailable:{no}
                A short summary.

            [1](subdoc/simple-1.qd)
            """.trimIndent(),
            outputResourceHook = { group ->
                val llmsTxt = findLlmsTxt(group)
                assertNotNull(llmsTxt)
                val content = llmsTxt.content.toString()
                // No .docname was set: the root falls back to Subdocument.Root.name ("index").
                assertContains(content, "[index](https://example.com)")
            },
        ) {}
    }

    @Test
    fun `llms_txt uses subdocument docname as link label`() {
        execute(
            """
            .htmloptions baseurl:{https://example.com}
            .doctype {docs}
            .docname {My site}

            .llmstxt markdownavailable:{yes}
                A short summary.

            [meta](subdoc/metadata.qd)
            """.trimIndent(),
            outputResourceHook = { group ->
                val llmsTxt = findLlmsTxt(group)
                assertNotNull(llmsTxt)
                val content = llmsTxt.content.toString()
                // metadata.qd sets `.docname {Subdocument name}`.
                assertContains(content, "[Subdocument name](https://example.com/metadata.md)")
            },
        ) {}
    }

    @Test
    fun `llms_txt preserves multi-line summary as blockquote`() {
        execute(
            """
            .htmloptions baseurl:{https://example.com}
            .doctype {docs}

            .llmstxt markdownavailable:{yes}
                First line.
                Second line.

            [1](subdoc/simple-1.qd)
            """.trimIndent(),
            outputResourceHook = { group ->
                val llmsTxt = findLlmsTxt(group)
                assertNotNull(llmsTxt)
                val content = llmsTxt.content.toString()
                assertContains(content, "> First line.")
                assertContains(content, "> Second line.")
            },
        ) {}
    }

    @Test
    fun `preview mode excludes llms_txt`() {
        execute(
            """
            .htmloptions baseurl:{https://example.com}
            .doctype {docs}

            .llmstxt markdownavailable:{yes}
                A short summary.

            [1](subdoc/simple-1.qd)
            """.trimIndent(),
            previewMode = true,
            outputResourceHook = { group ->
                assertNull(findLlmsTxt(group))
            },
        ) {}
    }

    // Search index

    private fun getSearchIndexOutputResource(group: OutputResource?): TextOutputArtifact {
        val resources = getSubResources(group)
        return resources.filterIsInstance<TextOutputArtifact>().first { it.name == "search-index" }
    }

    private fun getSearchIndexInternalResource(name: String): String =
        javaClass
            .getResource("/data/search-index/$name.json")!!
            .readText()
            .let { Json.parseToJsonElement(it).toString() }

    @Test
    fun `with search index, no headings, no metadata`() {
        execute(
            """
            .doctype {docs}
            
            [1](subdoc/simple-1.qd)
            [2](subdoc/simple-2.qd)
            """.trimIndent(),
            outputResourceHook = { group ->
                assertEquals(
                    getSearchIndexInternalResource("search-index-no-headings-no-metadata"),
                    getSearchIndexOutputResource(group).content,
                )
            },
        ) {}
    }

    @Test
    fun `with search index, no headings, with metadata`() {
        execute(
            """
            .docname {Test}
            .doctype {docs}
            
            [1](subdoc/simple-1.qd)
            [2](subdoc/metadata.qd)
            """.trimIndent(),
            outputResourceHook = { group ->
                assertEquals(
                    getSearchIndexInternalResource("search-index-no-headings-with-metadata"),
                    getSearchIndexOutputResource(group).content,
                )
            },
        ) {}
    }

    @Test
    fun `with search index, with headings`() {
        execute(
            """
            .docname {Test}
            .doctype {docs}
            
            [1](subdoc/headings-1.qd)
            [1](subdoc/headings-2.qd)
            """.trimIndent(),
            outputResourceHook = { group ->
                assertEquals(
                    getSearchIndexInternalResource("search-index-with-headings"),
                    getSearchIndexOutputResource(group).content,
                )
            },
        ) {}
    }

    @Test
    fun `with search index, with page margin`() {
        execute(
            """
            .docname {Test}
            .doctype {docs}
            .pagemargin {righttop}
                This is page margin content that should not appear in the search index
            
            # Heading
            
            Text
            """.trimIndent(),
            outputResourceHook = { group ->
                assertEquals(
                    getSearchIndexInternalResource("search-index-with-page-margin"),
                    getSearchIndexOutputResource(group).content,
                )
            },
        ) {}
    }

    // Preview mode

    @Test
    fun `preview mode excludes sitemap`() {
        execute(
            """
            .htmloptions baseurl:{https://example.com}
            .doctype {docs}

            [1](subdoc/simple-1.qd)
            """.trimIndent(),
            previewMode = true,
            outputResourceHook = { group ->
                assertNull(findSitemap(group))
            },
        ) {}
    }

    @Test
    fun `preview mode still includes theme and script`() {
        execute(
            source = "",
            previewMode = true,
            outputResourceHook = { group ->
                val resources = getSubResources(group).map { it.name }
                assertContains(resources, "theme")
                assertContains(resources, "script")
                assertContains(resources, "index")
            },
        ) {}
    }

    /**
     * Returns the [FileReferenceOutputArtifact]s nested inside the third-party `lib/` group,
     * which is where the symlink-vs-copy decision for dependencies is wired through.
     */
    private fun getThirdPartyLibraryArtifacts(group: OutputResource?): List<FileReferenceOutputArtifact> {
        val libGroup =
            getSubResources(group)
                .filterIsInstance<OutputResourceGroup>()
                .firstOrNull { it.name == "lib" }
        assertNotNull(libGroup, "Expected a 'lib' group with third-party dependencies")
        return libGroup.resources.filterIsInstance<FileReferenceOutputArtifact>()
    }

    /**
     * Returns the top-level `script` artifact.
     */
    private fun getScriptArtifact(group: OutputResource?): FileReferenceOutputArtifact {
        val script =
            getSubResources(group)
                .filterIsInstance<FileReferenceOutputArtifact>()
                .firstOrNull { it.name == "script" }
        assertNotNull(script)
        return script
    }

    @Test
    fun `preview mode marks third-party libraries and script as symlinks`() {
        execute(
            source = "",
            previewMode = true,
            outputResourceHook = { group ->
                val libs = getThirdPartyLibraryArtifacts(group)
                assertTrue(libs.isNotEmpty())
                assertTrue(libs.all { it.symlink })
                assertTrue(getScriptArtifact(group).symlink)
            },
        ) {}
    }

    @Test
    fun `non-preview mode copies third-party libraries and script instead of symlinking`() {
        execute(
            source = "",
            previewMode = false,
            outputResourceHook = { group ->
                val libs = getThirdPartyLibraryArtifacts(group)
                assertTrue(libs.isNotEmpty())
                assertTrue(libs.none { it.symlink })
                assertFalse(getScriptArtifact(group).symlink)
            },
        ) {}
    }
}
