package com.quarkdown.cli

import com.quarkdown.cli.creator.ProjectCreator
import com.quarkdown.cli.creator.content.DefaultProjectCreatorInitialContentSupplier
import com.quarkdown.cli.creator.content.EmptyProjectCreatorInitialContentSupplier
import com.quarkdown.cli.creator.template.DefaultProjectCreatorTemplateProcessorFactory
import com.quarkdown.core.document.DocumentAuthor
import com.quarkdown.core.document.DocumentInfo
import com.quarkdown.core.document.DocumentTheme
import com.quarkdown.core.document.DocumentType
import com.quarkdown.core.localization.LocaleLoader
import com.quarkdown.core.pipeline.output.OutputResource
import com.quarkdown.core.pipeline.output.OutputResourceGroup
import com.quarkdown.core.pipeline.output.TextOutputArtifact
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Tests for [ProjectCreator].
 */
class ProjectCreatorTest {
    private val OutputResource.textContent
        get() = (this as TextOutputArtifact).content

    private fun projectCreator(
        info: DocumentInfo,
        includeInitialContent: Boolean = false,
    ) = ProjectCreator(
        DefaultProjectCreatorTemplateProcessorFactory(info),
        if (includeInitialContent) DefaultProjectCreatorInitialContentSupplier() else EmptyProjectCreatorInitialContentSupplier(),
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
        resources.first().textContent.let {
            assertContains(it, ".doctype {paged}")
            assertContains(it, ".pagemargin {bottomcenter}")
        }
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
    }
}
