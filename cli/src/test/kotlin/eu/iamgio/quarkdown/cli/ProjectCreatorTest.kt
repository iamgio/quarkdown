package eu.iamgio.quarkdown.cli

import eu.iamgio.quarkdown.cli.creator.ProjectCreator
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

    @Test
    fun `only name`() {
        val creator = ProjectCreator(name = "Test")
        val resources = creator.createResources()
        assertEquals(1, resources.size)
        assertEquals(".docname {Test}", (resources.first() as TextOutputArtifact).content)
    }
}
