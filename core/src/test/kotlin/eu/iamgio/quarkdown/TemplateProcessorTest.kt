package eu.iamgio.quarkdown

import eu.iamgio.quarkdown.template.TemplateProcessor
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for [TemplateProcessor].
 */
class TemplateProcessorTest {
    @Test
    fun `no placeholders`() {
        val template = TemplateProcessor("Hello, world!")
        assertEquals("Hello, world!", template.build())
    }

    @Test
    fun empty() {
        val template = TemplateProcessor("")
        assertEquals("", template.build())
    }

    @Test
    fun `single placeholder`() {
        val template = TemplateProcessor("Hello, [[NAME]]!")
        template.value("NAME", "world")
        assertEquals("Hello, world!", template.build())
    }

    @Test
    fun `single placeholder not set`() {
        val template = TemplateProcessor("Hello, [[NAME]]!")
        assertEquals("Hello, [[NAME]]!", template.build())
    }

    @Test
    fun `single condition`() {
        val template = TemplateProcessor("Hello[[if:HASNAME]], world[[endif:HASNAME]]!")

        template.conditional("HASNAME", false)
        assertEquals("Hello!", template.build())

        template.conditional("HASNAME", true)
        assertEquals("Hello, world!", template.build())
    }

    @Test
    fun `single placeholder and single condition`() {
        val template = TemplateProcessor("Hello, [[NAME]]![[if:ASK]] How are you?[[endif:ASK]]")
        template.value("NAME", "world")

        template.conditional("ASK", false)
        assertEquals("Hello, world!", template.build())

        template.conditional("ASK", true)
        assertEquals("Hello, world! How are you?", template.build())
    }
}
