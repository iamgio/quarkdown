package com.quarkdown.cli

import com.quarkdown.cli.creator.ProjectCreator
import com.quarkdown.cli.creator.content.DefaultProjectCreatorInitialContentSupplier
import com.quarkdown.cli.creator.content.DocsProjectCreatorInitialContentSupplier
import com.quarkdown.cli.creator.content.EmptyProjectCreatorInitialContentSupplier
import com.quarkdown.cli.creator.content.PackageTemplateInitialContentSupplier
import com.quarkdown.cli.creator.content.ProjectCreatorInitialContentSupplier
import com.quarkdown.cli.creator.content.SlidesProjectCreatorInitialContentSupplier
import com.quarkdown.cli.creator.template.DefaultProjectCreatorTemplateProcessorFactory
import com.quarkdown.cli.creator.template.DocsProjectCreatorTemplateProcessorFactory
import com.quarkdown.core.document.DocumentAuthor
import com.quarkdown.core.document.DocumentInfo
import com.quarkdown.core.document.DocumentTheme
import com.quarkdown.core.document.DocumentType
import com.quarkdown.core.filesystem.VirtualFileSystem
import com.quarkdown.core.localization.LocaleLoader
import com.quarkdown.core.pipeline.output.FileReferenceOutputArtifact
import com.quarkdown.core.pipeline.output.OutputResource
import com.quarkdown.core.pipeline.output.OutputResourceGroup
import com.quarkdown.core.pipeline.output.TextOutputArtifact
import com.quarkdown.core.util.normalizeLineSeparators
import com.quarkdown.pm.template.PackageTemplate
import com.quarkdown.template.TemplateProcessor
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Tests for [ProjectCreator].
 */
class ProjectCreatorTest {
    private val OutputResource.textContent
        get() = (this as TextOutputArtifact).content.normalizeLineSeparators()

    private fun projectCreator(
        info: DocumentInfo,
        includeInitialContent: Boolean = false,
    ) = ProjectCreator(
        DefaultProjectCreatorTemplateProcessorFactory(info),
        if (includeInitialContent) DefaultProjectCreatorInitialContentSupplier() else EmptyProjectCreatorInitialContentSupplier(),
        mainFileName = "main",
    )

    private fun docsProjectCreator(
        info: DocumentInfo,
        includeInitialContent: Boolean = false,
    ) = ProjectCreator(
        DocsProjectCreatorTemplateProcessorFactory(info),
        if (includeInitialContent) DocsProjectCreatorInitialContentSupplier() else EmptyProjectCreatorInitialContentSupplier(),
        mainFileName = "main",
    )

    private fun slidesProjectCreator(info: DocumentInfo) =
        ProjectCreator(
            DefaultProjectCreatorTemplateProcessorFactory(info),
            SlidesProjectCreatorInitialContentSupplier(),
            mainFileName = "main",
        )

    @Test
    fun empty() {
        val creator = projectCreator(DocumentInfo())
        val resources = creator.createResources()
        assertEquals(1, resources.size)
        with(resources.first()) {
            assertEquals("main", name)
            assertIs<TextOutputArtifact>(this)
            assertEquals(".doctype {plain}", textContent)
        }
    }

    @Test
    fun `only name`() {
        val creator = projectCreator(DocumentInfo(name = "Test"))
        val resources = creator.createResources()
        assertEquals(1, resources.size)
        assertEquals(".docname {Test}\n.doctype {plain}", resources.first().textContent)
    }

    @Test
    fun `only description`() {
        val creator = projectCreator(DocumentInfo(description = "A sample document"))
        val resources = creator.createResources()
        assertEquals(1, resources.size)
        assertEquals(".docdescription {A sample document}\n.doctype {plain}", resources.first().textContent)
    }

    @Test
    fun `name and description`() {
        val creator = projectCreator(DocumentInfo(name = "Test", description = "A test document"))
        val resources = creator.createResources()
        assertEquals(1, resources.size)
        assertEquals(".docname {Test}\n.docdescription {A test document}\n.doctype {plain}", resources.first().textContent)
    }

    @Test
    fun `only keywords`() {
        val creator = projectCreator(DocumentInfo(keywords = listOf("kotlin", "testing")))
        val resources = creator.createResources()
        assertEquals(1, resources.size)
        assertEquals(
            ".doctype {plain}\n\n.dockeywords\n  - kotlin\n  - testing",
            resources.first().textContent,
        )
    }

    @Test
    fun `name and keywords`() {
        val creator = projectCreator(DocumentInfo(name = "Test", keywords = listOf("kotlin", "documentation")))
        val resources = creator.createResources()
        assertEquals(1, resources.size)
        assertEquals(
            ".docname {Test}\n.doctype {plain}\n\n.dockeywords\n  - kotlin\n  - documentation",
            resources.first().textContent,
        )
    }

    private val singleAuthor: MutableList<DocumentAuthor>
        get() = mutableListOf(DocumentAuthor("Giorgio"))

    @Test
    fun `only author`() {
        val creator = projectCreator(DocumentInfo(authors = singleAuthor))
        val resources = creator.createResources()
        assertEquals(1, resources.size)
        assertEquals(".doctype {plain}\n\n.docauthors\n  - Giorgio", resources.first().textContent)
    }

    @Test
    fun `name and author`() {
        val creator = projectCreator(DocumentInfo(name = "Document", authors = singleAuthor))
        val resources = creator.createResources()
        assertEquals(1, resources.size)
        assertEquals(".docname {Document}\n.doctype {plain}\n\n.docauthors\n  - Giorgio", resources.first().textContent)
    }

    @Test
    fun `description and author`() {
        val creator = projectCreator(DocumentInfo(description = "Test description", authors = singleAuthor))
        val resources = creator.createResources()
        assertEquals(1, resources.size)
        assertEquals(".docdescription {Test description}\n.doctype {plain}\n\n.docauthors\n  - Giorgio", resources.first().textContent)
    }

    @Test
    fun `multiple authors`() {
        val creator =
            projectCreator(DocumentInfo(authors = mutableListOf(DocumentAuthor("Giorgio"), DocumentAuthor("John"))))
        val resources = creator.createResources()
        assertEquals(1, resources.size)
        assertEquals(".doctype {plain}\n\n.docauthors\n  - Giorgio\n  - John", resources.first().textContent)
    }

    @Test
    fun `name and slides type`() {
        val creator = projectCreator(DocumentInfo(name = "Document", type = DocumentType.SLIDES))
        val resources = creator.createResources()
        assertEquals(1, resources.size)
        assertEquals(".docname {Document}\n.doctype {slides}", resources.first().textContent)
    }

    @Test
    fun `name and paged type`() {
        val creator = projectCreator(DocumentInfo(name = "Document", type = DocumentType.PAGED))
        val resources = creator.createResources()
        assertEquals(1, resources.size)
        assertEquals(".docname {Document}\n.doctype {paged}", resources.first().textContent)
    }

    @Test
    fun `paged initial content starts with the page counter`() {
        val creator = projectCreator(DocumentInfo(name = "Document", type = DocumentType.PAGED), includeInitialContent = true)
        val source = creator.createResources().first { it is TextOutputArtifact }.textContent
        assertTrue(
            source.startsWith(
                """
                .docname {Document}
                .doctype {paged}

                .pagemargin {bottomcenter}
                    .currentpage

                # Document
                """.trimIndent(),
            ),
        )
    }

    @Test
    fun `plain initial content has no page counter`() {
        val creator = projectCreator(DocumentInfo(name = "Document"), includeInitialContent = true)
        assertFalse(".pagemargin" in creator.createResources().first { it is TextOutputArtifact }.textContent)
    }

    @Test
    fun `only language`() {
        val creator = projectCreator(DocumentInfo(locale = LocaleLoader.SYSTEM.find("it")!!))
        val resources = creator.createResources()
        assertEquals(1, resources.size)
        assertEquals(".doctype {plain}\n.doclang {Italian}", resources.first().textContent)
    }

    @Test
    fun `full theme`() {
        val creator = projectCreator(DocumentInfo(theme = DocumentTheme(color = "dark", layout = "minimal")))
        val resources = creator.createResources()
        assertEquals(1, resources.size)
        assertEquals(".doctype {plain}\n.theme {dark} layout:{minimal}", resources.first().textContent)
    }

    @Test
    fun `only color theme`() {
        val creator = projectCreator(DocumentInfo(theme = DocumentTheme(color = "dark", layout = null)))
        val resources = creator.createResources()
        assertEquals(1, resources.size)
        assertEquals(".doctype {plain}\n.theme {dark}", resources.first().textContent)
    }

    @Test
    fun `only layout theme`() {
        val creator =
            projectCreator(
                DocumentInfo(theme = DocumentTheme(color = null, layout = "latex")),
            )
        val resources = creator.createResources()
        assertEquals(1, resources.size)
        assertEquals(".doctype {plain}\n.theme layout:{latex}", resources.first().textContent)
    }

    @Test
    fun `locale, theme and author`() {
        val creator =
            projectCreator(
                DocumentInfo(
                    locale = LocaleLoader.SYSTEM.find("en")!!,
                    theme = DocumentTheme(color = "dark", layout = "minimal"),
                    authors = singleAuthor,
                ),
            )
        val resources = creator.createResources()
        assertEquals(1, resources.size)
        assertEquals(
            """
            .doctype {plain}
            .doclang {English}
            .theme {dark} layout:{minimal}

            .docauthors
              - Giorgio
            """.trimIndent(),
            resources.first().textContent,
        )
    }

    @Test
    fun `name, description, keywords, locale, theme and author`() {
        val creator =
            projectCreator(
                DocumentInfo(
                    name = "Comprehensive Test",
                    description = "A comprehensive test document",
                    keywords = listOf("test", "kotlin", "quarkdown"),
                    locale = LocaleLoader.SYSTEM.find("en")!!,
                    theme = DocumentTheme(color = "dark", layout = "minimal"),
                    authors = singleAuthor,
                ),
            )
        val resources = creator.createResources()
        assertEquals(1, resources.size)
        assertEquals(
            """
            .docname {Comprehensive Test}
            .docdescription {A comprehensive test document}
            .doctype {plain}
            .doclang {English}
            .theme {dark} layout:{minimal}

            .dockeywords
              - test
              - kotlin
              - quarkdown

            .docauthors
              - Giorgio
            """.trimIndent(),
            resources.first().textContent,
        )
    }

    @Test
    fun `initial content`() {
        val creator = projectCreator(DocumentInfo(name = "Document"), includeInitialContent = true)
        val resources = creator.createResources()
        assertEquals(2, resources.size)

        val source = resources.first { it is TextOutputArtifact }
        val groups = resources.filterIsInstance<OutputResourceGroup>()
        assertEquals(1, groups.size)

        val images = groups.first { it.name == "image" }
        assertEquals("logo.png", images.resources.single().name)

        assertTrue(
            source.textContent.startsWith(
                """
                .docname {Document}
                .doctype {plain}

                # Document
                """.trimIndent(),
            ),
        )

        assertTrue("quarkdown c main.qd" in source.textContent)
        assertFalse("## Compiling" in source.textContent)
    }

    @Test
    fun `docs with name`() {
        val creator = docsProjectCreator(DocumentInfo(name = "Test", type = DocumentType.DOCS))
        val resources = creator.createResources()
        assertEquals(2, resources.size)

        val setup = resources.first { it.name == "_setup" }
        val main = resources.first { it.name == "main" }

        // _setup: name and doctype are excluded for docs.
        assertTrue(".docname {" !in setup.textContent)
        assertTrue(".doctype" !in setup.textContent)
        // _setup: has page margin with name.
        assertContains(setup.textContent, ".pagemargin {topleft}")
        assertContains(setup.textContent, "Test")
        assertContains(setup.textContent, ".htmloptions title:{.docname | Test}")

        // main: has .docname and .include {docs}.
        assertContains(main.textContent, ".docname {Test}")
        assertContains(main.textContent, ".include {docs}")
    }

    @Test
    fun `docs with name and description`() {
        val creator =
            docsProjectCreator(
                DocumentInfo(name = "Test", description = "A test document", type = DocumentType.DOCS),
            )
        val resources = creator.createResources()
        assertEquals(2, resources.size)

        val setup = resources.first { it.name == "_setup" }
        val main = resources.first { it.name == "main" }

        // _setup: has description and page margin, but not .docname or .doctype.
        assertContains(setup.textContent, ".docdescription {A test document}")
        assertContains(setup.textContent, ".pagemargin {topleft}")
        assertTrue(".docname {" !in setup.textContent)
        assertTrue(".docname" in setup.textContent)
        assertTrue(".doctype" !in setup.textContent)

        // main: has .docname and .include {docs}.
        assertContains(main.textContent, ".docname {Test}")
        assertContains(main.textContent, ".include {docs}")
    }

    @Test
    fun `docs with name, author and keywords`() {
        val creator =
            docsProjectCreator(
                DocumentInfo(
                    name = "Test",
                    type = DocumentType.DOCS,
                    authors = singleAuthor,
                    keywords = listOf("kotlin", "docs"),
                ),
            )
        val resources = creator.createResources()
        assertEquals(2, resources.size)

        val setup = resources.first { it.name == "_setup" }

        // _setup: has authors, keywords, and page margin.
        assertContains(setup.textContent, ".docauthors")
        assertContains(setup.textContent, "- Giorgio")
        assertContains(setup.textContent, ".dockeywords")
        assertContains(setup.textContent, "- kotlin")
        assertContains(setup.textContent, "- docs")
        assertContains(setup.textContent, ".pagemargin {topleft}")

        // _setup: no .docname or .doctype for docs.
        assertTrue(".docname {" !in setup.textContent)
        assertTrue(".docname" in setup.textContent)
        assertTrue(".doctype" !in setup.textContent)
    }

    @Test
    fun `docs with initial content`() {
        val creator =
            docsProjectCreator(
                DocumentInfo(name = "Test", type = DocumentType.DOCS),
                includeInitialContent = true,
            )
        val resources = creator.createResources()

        // 2 text outputs (_setup, main) + 4 docs resource files.
        assertEquals(6, resources.size)

        val main = resources.first { it.name == "main" }
        assertContains(main.textContent, ".docname {Test}")
        assertContains(main.textContent, ".include {docs}")

        // Initial content for docs has specific sections.
        assertContains(main.textContent, "\n## Compiling\n")
        assertContains(main.textContent, "quarkdown c main.qd")
        assertContains(main.textContent, "\n## Structure\n")
        assertContains(main.textContent, "_setup.qd")

        // Initial content for docs does not have a heading or an image.
        assertTrue("# Test" !in main.textContent)
        assertTrue("logo.png" !in main.textContent)

        // The docs resource files.
        assertTrue(resources.any { it.name == "_nav.qd" })
        assertTrue(resources.any { it.name == "page-1.qd" })
        assertTrue(resources.any { it.name == "page-2.qd" })
        assertTrue(resources.any { it.name == "page-3.qd" })
    }

    @Test
    fun `slides with initial content`() {
        val creator =
            slidesProjectCreator(
                DocumentInfo(name = "Test", description = "My slides", type = DocumentType.SLIDES),
            )
        val resources = creator.createResources()
        assertEquals(2, resources.size)

        // The logo image is supplied, like in default projects.
        val images = resources.filterIsInstance<OutputResourceGroup>().single { it.name == "image" }
        assertEquals("logo.png", images.resources.single().name)

        val main = resources.first { it is TextOutputArtifact }
        val content = main.textContent

        assertContains(content, ".docname {Test}")
        assertContains(content, ".doctype {slides}")

        // Slide-specific structure.
        assertContains(content, ".footer\n    .docauthor\n\n    .docname\n\n    .currentpage / .totalpages")
        assertContains(content, "# .docname")
        assertContains(content, ".text {.docdescription} variant:{smallcaps}")
        assertContains(content, "\n## First slide\n")
        assertContains(content, "\n## Second slide\n")

        // Shared getting-started content.
        assertContains(content, "quarkdown c main.qd")
        assertContains(content, "official wiki")

        // The second slide lays out text and a formula in a row, followed by the logo.
        assertContains(content, ".row alignment:{spacebetween}\n    This is a multi-column layout.")
        assertContains(content, "!(25%)[Quarkdown](image/logo.png)")

        // No docs sections or heading with raw name.
        assertFalse("## Compiling" in content)
        assertFalse("# Test" in content)
    }

    @Test
    fun `supplier content is injected verbatim`() {
        val supplier =
            object : ProjectCreatorInitialContentSupplier {
                override fun createInitialContent(template: TemplateProcessor): CharSequence = "# Hello\n\nFrom a supplier.\n"

                override fun createResources(): Set<OutputResource> = emptySet()
            }
        val creator = ProjectCreator(DefaultProjectCreatorTemplateProcessorFactory(DocumentInfo(name = "Doc")), supplier, "main")
        assertEquals(".docname {Doc}\n.doctype {plain}\n\n# Hello\n\nFrom a supplier.", creator.createResources().single().textContent)
    }

    @Test
    fun `includes render after the theme and before authors`() {
        val creator =
            ProjectCreator(
                DefaultProjectCreatorTemplateProcessorFactory(
                    DocumentInfo(theme = DocumentTheme(color = "dark", layout = "minimal"), authors = singleAuthor),
                    includes = listOf("@atoms/cv:1.0.0", "@atoms/extra:2.0.0"),
                ),
                EmptyProjectCreatorInitialContentSupplier(),
                mainFileName = "main",
            )
        assertEquals(
            """
            .doctype {plain}
            .theme {dark} layout:{minimal}
            .include {@atoms/cv:1.0.0}
            .include {@atoms/extra:2.0.0}

            .docauthors
              - Giorgio
            """.trimIndent(),
            creator.createResources().single().textContent,
        )
    }

    @Test
    fun `docs includes render in the setup file`() {
        val creator =
            ProjectCreator(
                DocsProjectCreatorTemplateProcessorFactory(
                    DocumentInfo(name = "Test", type = DocumentType.DOCS),
                    includes = listOf("@atoms/cv:1.0.0"),
                ),
                EmptyProjectCreatorInitialContentSupplier(),
                mainFileName = "main",
            )
        val resources = creator.createResources()
        assertContains(resources.first { it.name == "_setup" }.textContent, ".include {@atoms/cv:1.0.0}")
        assertFalse("@atoms" in resources.first { it.name == "main" }.textContent)
    }

    private fun packageTemplateCreator(
        content: String,
        additionalFiles: Map<String, String> = emptyMap(),
    ): ProjectCreator {
        val files = VirtualFileSystem().apply { additionalFiles.forEach { (path, text) -> writeBytes(path, text.encodeToByteArray()) } }
        val template = PackageTemplate("@atoms/cv:1.0.0", doctype = null, content = content, additionalFiles = files.resolve("/"))
        return ProjectCreator(
            DefaultProjectCreatorTemplateProcessorFactory(DocumentInfo(name = "CV"), includes = listOf(template.reference)),
            PackageTemplateInitialContentSupplier(template),
            mainFileName = "main",
        )
    }

    @Test
    fun `package template content is injected verbatim`() {
        val resources = packageTemplateCreator("# .docname\n\n.cv {${'$'}{name}}\n").createResources()
        assertEquals(
            ".docname {CV}\n.doctype {plain}\n.include {@atoms/cv:1.0.0}\n\n# .docname\n\n.cv {${'$'}{name}}",
            resources.single().textContent,
        )
    }

    @Test
    fun `empty content yields no initial content`() {
        val resources = packageTemplateCreator("").createResources()
        assertEquals(".docname {CV}\n.doctype {plain}\n.include {@atoms/cv:1.0.0}", resources.single().textContent)
    }

    @Test
    fun `additional files become file references at their paths`() {
        val resources =
            packageTemplateCreator(
                "# CV",
                mapOf(
                    "sections/experience.qd" to "# Experience",
                    "sections/assets/My Photo.png" to "png",
                    ".gitignore" to "output/",
                ),
            ).createResources()

        val files = resources.filterIsInstance<FileReferenceOutputArtifact>()
        assertEquals(setOf("sections/experience.qd", "sections/assets/My Photo.png", ".gitignore"), files.map { it.name }.toSet())
        assertEquals("output/", files.first { it.name == ".gitignore" }.file.readText())
        assertEquals("# Experience", files.first { it.name == "sections/experience.qd" }.file.readText())
    }
}
