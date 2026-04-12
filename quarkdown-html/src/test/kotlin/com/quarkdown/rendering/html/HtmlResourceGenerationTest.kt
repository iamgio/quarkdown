package com.quarkdown.rendering.html

import com.quarkdown.core.ast.attributes.MutableAstAttributes
import com.quarkdown.core.ast.attributes.presence.markCodePresence
import com.quarkdown.core.ast.attributes.presence.markMathPresence
import com.quarkdown.core.ast.attributes.presence.markMermaidDiagramPresence
import com.quarkdown.core.attachMockPipeline
import com.quarkdown.core.context.MutableContext
import com.quarkdown.core.context.SubdocumentContext
import com.quarkdown.core.document.DocumentInfo
import com.quarkdown.core.document.DocumentTheme
import com.quarkdown.core.document.DocumentType
import com.quarkdown.core.document.sub.Subdocument
import com.quarkdown.core.flavor.quarkdown.QuarkdownFlavor
import com.quarkdown.core.localization.LocaleLoader
import com.quarkdown.core.media.storage.MEDIA_SUBDIRECTORY_NAME
import com.quarkdown.core.permissions.Permission
import com.quarkdown.core.pipeline.PipelineOptions
import com.quarkdown.core.pipeline.output.ArtifactType
import com.quarkdown.core.pipeline.output.FileReferenceOutputArtifact
import com.quarkdown.core.pipeline.output.OutputResource
import com.quarkdown.core.pipeline.output.OutputResourceGroup
import com.quarkdown.core.pipeline.output.TextOutputArtifact
import com.quarkdown.installlayout.InstallLayout
import com.quarkdown.installlayout.InstallLayoutDirectory
import com.quarkdown.rendering.html.post.HtmlPostRenderer
import com.quarkdown.rendering.html.post.resources.HTML_LIBRARY_OUTPUT_PATH
import com.quarkdown.rendering.html.post.resources.ThemePostRendererResource
import com.quarkdown.rendering.html.post.resources.ThirdPartyPostRendererResource
import com.quarkdown.rendering.html.post.thirdparty.ThirdPartyLibrary
import java.io.File
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for HTML resource generation, including themes, scripts, media, and third-party library bundling.
 */
class HtmlResourceGenerationTest {
    private lateinit var context: MutableContext

    /**
     * The real HTML resources layout, resolved from the dev-time install layout
     * (populated by the `:assembleDevLib` Gradle task).
     */
    private val htmlResources: InstallLayout.Html = InstallLayout.get.htmlResources

    @BeforeTest
    fun setup() {
        context = MutableContext(QuarkdownFlavor)
        context.attachMockPipeline(
            PipelineOptions(
                permissions = setOf(Permission.ProjectRead, Permission.GlobalRead, Permission.NetworkAccess),
            ),
        )
    }

    private fun postRenderer(resourcesLayout: InstallLayout.Html? = null): HtmlPostRenderer =
        HtmlPostRenderer(context, resourcesLayout = resourcesLayout)

    // Themes

    private fun assertThemeGroupContains(
        resources: Set<OutputResource>,
        expectedThemes: Set<String>,
        notExpectedThemes: Set<String> = emptySet(),
    ) {
        val themeGroup = resources.filterIsInstance<OutputResourceGroup>().first { it.name == "theme" }
        val themes = themeGroup.resources.map { it.name }.toSet()
        expectedThemes.forEach { assertTrue(it in themes, "Expected theme entry '$it' in $themes") }
        notExpectedThemes.forEach { assertFalse(it in themes, "Unexpected theme entry '$it' in $themes") }

        val theme = themeGroup.resources.first { it.name == "theme" } as TextOutputArtifact
        assertEquals(ArtifactType.CSS, theme.type)
        expectedThemes.filter { it != "theme" }.forEach {
            val importPath = ThemePostRendererResource.importPathFor(it)
            assertTrue(
                "@import url('$importPath');" in theme.content,
                "Expected '@import url('$importPath');' in ${theme.content}",
            )
        }
    }

    private val plainHtml = "<html><head></head><body></body></html>"

    private fun `generate resources`(
        documentInfo: DocumentInfo =
            DocumentInfo(
                type = DocumentType.SLIDES,
                theme = DocumentTheme(color = "darko", layout = "minimal"),
            ),
        expectedThemes: Set<String> = setOf("color/darko", "layout/minimal", "global.css", "theme"),
        notExpectedThemes: Set<String> = emptySet(),
        initAttributes: MutableAstAttributes.() -> Unit = { markMathPresence() },
        block: (Set<OutputResource>) -> Unit,
    ) {
        context.documentInfo = documentInfo
        context.attributes.initAttributes()

        // Theme-sensitive generation requires the real bundled themes directory to look up
        // per-theme folders on disk.
        val postRenderer = postRenderer(resourcesLayout = htmlResources)
        val resources = postRenderer.generateResources(plainHtml)

        block(resources)

        resources.filterIsInstance<TextOutputArtifact>().first { it.type == ArtifactType.HTML }.let {
            assertEquals(plainHtml, it.content)
        }

        assertThemeGroupContains(resources, expectedThemes, notExpectedThemes)

        // The script directory is emitted as a single FileReferenceOutputArtifact pointing to
        // the `script/` directory in the install layout.
        val scriptArtifact =
            resources.filterIsInstance<FileReferenceOutputArtifact>().first { it.name == "script" }
        assertTrue(scriptArtifact.file.isDirectory)
        assertTrue(scriptArtifact.file.resolve("quarkdown.min.js").isFile)
        assertTrue(scriptArtifact.file.resolve("quarkdown.min.js.map").isFile)
    }

    @Test
    fun `no media`() =
        `generate resources` { resources ->
            // theme + script + lib + HTML
            assertEquals(4, resources.size)
            assertFalse(MEDIA_SUBDIRECTORY_NAME in resources.map { it.name }) // Media storage is empty.
        }

    @Test
    fun `with media`() {
        context.options.enableLocalMediaStorage = true
        context.mediaStorage.register("src/test/resources/media/file.txt", workingDirectory = null)
        `generate resources` { resources ->
            // theme + script + lib + media + HTML
            assertEquals(5, resources.size)
            assertTrue(MEDIA_SUBDIRECTORY_NAME in resources.map { it.name })
        }
    }

    @Test
    fun `default theme`() =
        `generate resources`(
            documentInfo = DocumentInfo(),
            expectedThemes = setOf("color/paperwhite", "layout/latex", "global.css", "theme"),
            notExpectedThemes = setOf("locale/zh"),
        ) { resources ->
            assertEquals(4, resources.size)
        }

    @Test
    fun `with specific localized theme`() =
        `generate resources`(
            documentInfo = DocumentInfo(locale = LocaleLoader.SYSTEM.find("zh-CN")),
            expectedThemes = setOf("color/paperwhite", "layout/latex", "global.css", "theme", "locale/zh"),
        ) { resources ->
            assertEquals(4, resources.size)
        }

    @Test
    fun `with missing localized theme`() =
        `generate resources`(
            documentInfo = DocumentInfo(locale = LocaleLoader.SYSTEM.find("akan")),
            expectedThemes = setOf("color/paperwhite", "layout/latex", "global.css", "theme"),
            notExpectedThemes = setOf("locale/akan"),
        ) { resources ->
            assertEquals(4, resources.size)
        }

    @Test
    fun `no theme group when library directory is null`() {
        context.documentInfo = DocumentInfo()
        val resources = postRenderer(resourcesLayout = null).generateResources(plainHtml)
        assertNull(resources.filterIsInstance<OutputResourceGroup>().firstOrNull { it.name == "theme" })
    }

    @Test
    fun `no script artifact when library directory is null`() {
        context.documentInfo = DocumentInfo()
        val resources = postRenderer(resourcesLayout = null).generateResources(plainHtml)
        assertNull(resources.filterIsInstance<FileReferenceOutputArtifact>().firstOrNull { it.name == "script" })
    }

    @Test
    fun `layout theme artifact exposes exported asset folder`() {
        context.documentInfo =
            DocumentInfo(
                type = DocumentType.SLIDES,
                theme = DocumentTheme(color = "beaver", layout = "beamer"),
            )
        val resources = postRenderer(resourcesLayout = htmlResources).generateResources(plainHtml)
        val themeGroup = resources.filterIsInstance<OutputResourceGroup>().first { it.name == "theme" }
        val beamerArtifact =
            themeGroup.resources
                .filterIsInstance<FileReferenceOutputArtifact>()
                .first { it.name == "layout/beamer" }

        assertTrue(beamerArtifact.file.isDirectory)
        // Fonts declared by `src/main/scss/layout/beamer.json` must ship alongside the CSS.
        assertTrue(
            beamerArtifact.file.resolve("source-sans-pro").isDirectory,
            "Expected beamer theme to include the 'source-sans-pro' export directory.",
        )
    }

    // Third-party libraries

    /**
     * Finds the `lib` [OutputResourceGroup] in the given resources, or `null` if absent.
     */
    private fun Set<OutputResource>.findLibGroup(): OutputResourceGroup? =
        filterIsInstance<OutputResourceGroup>().firstOrNull { it.name == HTML_LIBRARY_OUTPUT_PATH }

    /**
     * Names of the library directories bundled under the `lib/` group.
     */
    private val OutputResourceGroup.libraryNames: Set<String>
        get() = resources.map { it.name }.toSet()

    /**
     * Generates resources with the real bundled [htmlResources] and returns the `lib` group.
     */
    private fun generateLibGroup(): OutputResourceGroup? {
        val resources = postRenderer(resourcesLayout = htmlResources).generateResources(plainHtml)
        return resources.findLibGroup()
    }

    @Test
    fun `no lib group when library directory is null`() {
        val resources = postRenderer(resourcesLayout = null).generateResources(plainHtml)
        assertNull(resources.findLibGroup())
    }

    @Test
    fun `no lib group when library directory does not exist`() {
        val bogusLibDir = InstallLayoutDirectory(File("nonexistent"))
        val resources =
            buildSet {
                ThirdPartyPostRendererResource(context, librariesLayout = bogusLibDir)
                    .includeTo(this, plainHtml)
            }
        assertNull(resources.filterIsInstance<OutputResourceGroup>().firstOrNull { it.name == HTML_LIBRARY_OUTPUT_PATH })
    }

    @Test
    fun `lib group always includes bootstrap-icons`() {
        context.documentInfo = DocumentInfo(type = DocumentType.PLAIN)
        val libGroup = generateLibGroup()

        assertNotNull(libGroup)
        assertTrue("bootstrap-icons" in libGroup.libraryNames)
    }

    @Test
    fun `lib group includes katex when math is present`() {
        context.documentInfo = DocumentInfo(type = DocumentType.PLAIN)
        context.attributes.markMathPresence()
        val libGroup = generateLibGroup()

        assertNotNull(libGroup)
        assertTrue("katex" in libGroup.libraryNames)
    }

    @Test
    fun `lib group excludes katex when no math`() {
        context.documentInfo = DocumentInfo(type = DocumentType.PLAIN)
        val libGroup = generateLibGroup()

        assertNotNull(libGroup)
        assertFalse("katex" in libGroup.libraryNames)
    }

    @Test
    fun `lib group includes highlightjs when code is present`() {
        context.documentInfo = DocumentInfo(type = DocumentType.PLAIN)
        context.attributes.markCodePresence()
        val libGroup = generateLibGroup()

        assertNotNull(libGroup)
        ThirdPartyLibrary.HighlightJs.names.forEach {
            assertTrue(it in libGroup.libraryNames)
        }
    }

    @Test
    fun `lib group excludes highlightjs when no code`() {
        context.documentInfo = DocumentInfo(type = DocumentType.PLAIN)
        val libGroup = generateLibGroup()

        assertNotNull(libGroup)
        ThirdPartyLibrary.HighlightJs.names.forEach {
            assertFalse(it in libGroup.libraryNames)
        }
    }

    @Test
    fun `lib group includes mermaid when diagrams are present`() {
        context.documentInfo = DocumentInfo(type = DocumentType.PLAIN)
        context.attributes.markMermaidDiagramPresence()
        val libGroup = generateLibGroup()

        assertNotNull(libGroup)
        assertTrue("mermaid" in libGroup.libraryNames)
    }

    @Test
    fun `lib group excludes mermaid when no diagrams`() {
        context.documentInfo = DocumentInfo(type = DocumentType.PLAIN)
        val libGroup = generateLibGroup()

        assertNotNull(libGroup)
        assertFalse("mermaid" in libGroup.libraryNames)
    }

    @Test
    fun `lib group includes reveal-js for slides`() {
        context.documentInfo = DocumentInfo(type = DocumentType.SLIDES)
        val libGroup = generateLibGroup()

        assertNotNull(libGroup)
        assertTrue("reveal.js" in libGroup.libraryNames)
    }

    @Test
    fun `lib group excludes reveal-js for non-slides`() {
        context.documentInfo = DocumentInfo(type = DocumentType.PLAIN)
        val libGroup = generateLibGroup()

        assertNotNull(libGroup)
        assertFalse("reveal.js" in libGroup.libraryNames)
    }

    @Test
    fun `lib group includes paged-js for paged documents`() {
        context.documentInfo = DocumentInfo(type = DocumentType.PAGED)
        val libGroup = generateLibGroup()

        assertNotNull(libGroup)
        assertTrue("pagedjs" in libGroup.libraryNames)
    }

    @Test
    fun `lib group excludes paged-js for non-paged documents`() {
        context.documentInfo = DocumentInfo(type = DocumentType.PLAIN)
        val libGroup = generateLibGroup()

        assertNotNull(libGroup)
        assertFalse("pagedjs" in libGroup.libraryNames)
    }

    @Test
    fun `lib entries are FileReferenceOutputArtifacts`() {
        context.documentInfo = DocumentInfo(type = DocumentType.PLAIN)
        val libGroup = generateLibGroup()

        assertNotNull(libGroup)
        libGroup.resources.forEach { resource ->
            assertTrue(resource is FileReferenceOutputArtifact)
        }
    }

    @Test
    fun `slides bundle includes reveal-js and excludes paged-js`() {
        context.documentInfo = DocumentInfo(type = DocumentType.SLIDES)
        val libGroup = generateLibGroup()

        assertNotNull(libGroup)
        assertTrue("reveal.js" in libGroup.libraryNames)
        assertTrue("bootstrap-icons" in libGroup.libraryNames)
        assertFalse("pagedjs" in libGroup.libraryNames)
    }

    @Test
    fun `paged bundle includes paged-js and excludes reveal-js`() {
        context.documentInfo = DocumentInfo(type = DocumentType.PAGED)
        val libGroup = generateLibGroup()

        assertNotNull(libGroup)
        assertTrue("pagedjs" in libGroup.libraryNames)
        assertTrue("bootstrap-icons" in libGroup.libraryNames)
        assertFalse("reveal.js" in libGroup.libraryNames)
    }

    @Test
    fun `plain bundle includes only bootstrap-icons when no content features`() {
        context.documentInfo = DocumentInfo(type = DocumentType.PLAIN)
        val libGroup = generateLibGroup()

        assertNotNull(libGroup)
        assertEquals(setOf("bootstrap-icons"), libGroup.libraryNames)
    }

    /**
     * Registers a new [SubdocumentContext] forked from the root [context] and adds it to the shared
     * subdocuments data, so that it is visible to [com.quarkdown.rendering.html.post.resources.ThirdPartyPostRendererResource].
     */
    private fun addSubdocumentContext(name: String): SubdocumentContext {
        val subdocument = Subdocument.Resource(name = name, path = name, content = "")
        val subContext = SubdocumentContext(parent = context, subdocument = subdocument)
        context.sharedSubdocumentsData = context.sharedSubdocumentsData.addContext(subdocument, subContext)
        return subContext
    }

    @Test
    fun `lib group includes libraries required by subdocuments`() {
        context.documentInfo = DocumentInfo(type = DocumentType.DOCS)
        // Root has no code, math, or diagrams, but subdocuments do.
        val codeSub = addSubdocumentContext("code-sub")
        codeSub.attributes.markCodePresence()
        val mathSub = addSubdocumentContext("math-sub")
        mathSub.attributes.markMathPresence()

        val libGroup = generateLibGroup()

        assertNotNull(libGroup)
        assertTrue("bootstrap-icons" in libGroup.libraryNames)
        assertTrue("katex" in libGroup.libraryNames)
        ThirdPartyLibrary.HighlightJs.names.forEach {
            assertTrue(it in libGroup.libraryNames)
        }
    }

    @Test
    fun `all libraries included when all features are present in slides`() {
        context.documentInfo = DocumentInfo(type = DocumentType.SLIDES)
        context.attributes.markMathPresence()
        context.attributes.markCodePresence()
        context.attributes.markMermaidDiagramPresence()
        val libGroup = generateLibGroup()

        assertNotNull(libGroup)
        val expectedNames =
            ThirdPartyLibrary
                .all()
                .filter { it !is ThirdPartyLibrary.PagedJs }
                .flatMap { it.names }
                .toSet()
        assertEquals(expectedNames, libGroup.libraryNames)
    }
}
