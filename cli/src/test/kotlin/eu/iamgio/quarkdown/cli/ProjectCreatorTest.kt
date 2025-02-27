package eu.iamgio.quarkdown.cli

import eu.iamgio.quarkdown.cli.creator.ProjectCreator
import eu.iamgio.quarkdown.document.DocumentType
import eu.iamgio.quarkdown.localization.LocaleLoader
import eu.iamgio.quarkdown.pipeline.output.OutputResource
import eu.iamgio.quarkdown.pipeline.output.TextOutputArtifact
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Tests for [ProjectCreator].
 */
class ProjectCreatorTest {
    @Test
    fun empty() {
        val creator = ProjectCreator()
        val resources = creator.createResources()
        assertEquals(1, resources.size)
        with(resources.first()) {
            assertEquals("main.qmd", name)
            assertIs<TextOutputArtifact>(this)
            assertTrue(content.isEmpty())
        }
    }

    private val OutputResource.textContent
        get() = (this as TextOutputArtifact).content

    @Test
    fun `only name`() {
        val creator = ProjectCreator(name = "Test")
        val resources = creator.createResources()
        assertEquals(1, resources.size)
        assertEquals(".docname {Test}", resources.first().textContent)
    }

    @Test
    fun `only author`() {
        val creator = ProjectCreator(author = "Giorgio")
        val resources = creator.createResources()
        assertEquals(1, resources.size)
        assertEquals(".docauthor {Giorgio}", resources.first().textContent)
    }

    @Test
    fun `name and author`() {
        val creator = ProjectCreator(name = "Document", author = "Giorgio")
        val resources = creator.createResources()
        assertEquals(1, resources.size)
        assertEquals(".docname {Document}\n.docauthor {Giorgio}", resources.first().textContent)
    }

    @Test
    fun `author and type`() {
        val creator = ProjectCreator(author = "Giorgio", type = DocumentType.SLIDES)
        val resources = creator.createResources()
        assertEquals(1, resources.size)
        assertEquals(".docauthor {Giorgio}\n.doctype {slides}", resources.first().textContent)
    }

    @Test
    fun `name and type`() {
        val creator = ProjectCreator(name = "Document", type = DocumentType.SLIDES)
        val resources = creator.createResources()
        assertEquals(1, resources.size)
        assertEquals(".docname {Document}\n.doctype {slides}", resources.first().textContent)
    }

    @Test
    fun `only language`() {
        val creator = ProjectCreator(language = LocaleLoader.SYSTEM.find("it")!!)
        val resources = creator.createResources()
        assertEquals(1, resources.size)
        assertEquals(".doclang {Italian}", resources.first().textContent)
    }
}
