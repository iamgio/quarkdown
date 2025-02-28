package eu.iamgio.quarkdown.cli

import eu.iamgio.quarkdown.cli.creator.ProjectCreator
import eu.iamgio.quarkdown.document.DocumentAuthor
import eu.iamgio.quarkdown.document.DocumentInfo
import eu.iamgio.quarkdown.document.DocumentTheme
import eu.iamgio.quarkdown.document.DocumentType
import eu.iamgio.quarkdown.localization.LocaleLoader
import eu.iamgio.quarkdown.pipeline.output.OutputResource
import eu.iamgio.quarkdown.pipeline.output.TextOutputArtifact
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

/**
 * Tests for [ProjectCreator].
 */
class ProjectCreatorTest {
    private val OutputResource.textContent
        get() = (this as TextOutputArtifact).content

    @Test
    fun empty() {
        val creator = ProjectCreator(DocumentInfo())
        val resources = creator.createResources()
        assertEquals(1, resources.size)
        with(resources.first()) {
            assertEquals("main.qmd", name)
            assertIs<TextOutputArtifact>(this)
            assertEquals(".doctype {plain}", textContent)
        }
    }

    @Test
    fun `only name`() {
        val creator = ProjectCreator(DocumentInfo(name = "Test"))
        val resources = creator.createResources()
        assertEquals(1, resources.size)
        assertEquals(".docname {Test}\n.doctype {plain}", resources.first().textContent)
    }

    private val singleAuthor: MutableList<DocumentAuthor>
        get() = mutableListOf(DocumentAuthor("Giorgio"))

    @Test
    fun `only author`() {
        val creator = ProjectCreator(DocumentInfo(authors = singleAuthor))
        val resources = creator.createResources()
        assertEquals(1, resources.size)
        assertEquals(".doctype {plain}\n\n.docauthors\n  - Giorgio", resources.first().textContent)
    }

    @Test
    fun `name and author`() {
        val creator = ProjectCreator(DocumentInfo(name = "Document", authors = singleAuthor))
        val resources = creator.createResources()
        assertEquals(1, resources.size)
        assertEquals(".docname {Document}\n.doctype {plain}\n\n.docauthors\n  - Giorgio", resources.first().textContent)
    }

    @Test
    fun `multiple authors`() {
        val creator =
            ProjectCreator(DocumentInfo(authors = mutableListOf(DocumentAuthor("Giorgio"), DocumentAuthor("John"))))
        val resources = creator.createResources()
        assertEquals(1, resources.size)
        assertEquals(".doctype {plain}\n\n.docauthors\n  - Giorgio\n  - John", resources.first().textContent)
    }

    @Test
    fun `name and type`() {
        val creator = ProjectCreator(DocumentInfo(name = "Document", type = DocumentType.SLIDES))
        val resources = creator.createResources()
        assertEquals(1, resources.size)
        assertEquals(".docname {Document}\n.doctype {slides}", resources.first().textContent)
    }

    @Test
    fun `only language`() {
        val creator = ProjectCreator(DocumentInfo(locale = LocaleLoader.SYSTEM.find("it")!!))
        val resources = creator.createResources()
        assertEquals(1, resources.size)
        assertEquals(".doctype {plain}\n.doclang {Italian}", resources.first().textContent)
    }

    @Test
    fun `full theme`() {
        val creator = ProjectCreator(DocumentInfo(theme = DocumentTheme(color = "dark", layout = "minimal")))
        val resources = creator.createResources()
        assertEquals(1, resources.size)
        assertEquals(".doctype {plain}\n.theme {dark} layout:{minimal}", resources.first().textContent)
    }

    @Test
    fun `only color theme`() {
        val creator = ProjectCreator(DocumentInfo(theme = DocumentTheme(color = "dark", layout = null)))
        val resources = creator.createResources()
        assertEquals(1, resources.size)
        assertEquals(".doctype {plain}\n.theme {dark}", resources.first().textContent)
    }

    @Test
    fun `only layout theme`() {
        val creator =
            ProjectCreator(
                DocumentInfo(theme = DocumentTheme(color = null, layout = "latex")),
            )
        val resources = creator.createResources()
        assertEquals(1, resources.size)
        assertEquals(".doctype {plain}\n.theme layout:{latex}", resources.first().textContent)
    }

    @Test
    fun `locale, theme and author`() {
        val creator =
            ProjectCreator(
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
}
