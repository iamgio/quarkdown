package eu.iamgio.quarkdown

import eu.iamgio.quarkdown.template.TemplateProcessor
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for [TemplateProcessor].
 */
class TemplateProcessorTest {
    @Test
    fun `no values`() {
        val template = TemplateProcessor("Hello, world!")
        assertEquals("Hello, world!", template.process())
    }

    @Test
    fun empty() {
        val template = TemplateProcessor("")
        assertEquals("", template.process())
    }

    @Test
    fun `single value`() {
        val template = TemplateProcessor("Hello, [[NAME]]!")
        template.value("NAME", "world")
        assertEquals("Hello, world!", template.process())
    }

    @Test
    fun `double value`() {
        val template = TemplateProcessor("Hello, [[NAME]] from [[FROM]]!")
        template.value("NAME", "world")
        template.value("FROM", "Quarkdown")
        assertEquals("Hello, world from Quarkdown!", template.process())
    }

    @Test
    fun `single value not set`() {
        val template = TemplateProcessor("Hello, [[NAME]]!")
        assertEquals("Hello, [[NAME]]!", template.process())
    }

    @Test
    fun `single condition`() {
        val template = TemplateProcessor("Hello[[if:HASNAME]], world[[endif:HASNAME]]!")

        template.conditional("HASNAME", false)
        assertEquals("Hello!", template.process())

        template.conditional("HASNAME", true)
        assertEquals("Hello, world!", template.process())
    }

    @Test
    fun `double condition`() {
        val template =
            TemplateProcessor("Hello[[if:HASNAME]], world[[endif:HASNAME]]![[if:ASK]] How are you?[[endif:ASK]]")

        template.conditional("HASNAME", false)
        template.conditional("ASK", false)
        assertEquals("Hello!", template.process())

        template.conditional("ASK", true)
        assertEquals("Hello! How are you?", template.process())

        template.conditional("HASNAME", true)
        assertEquals("Hello, world! How are you?", template.process())

        template.conditional("ASK", false)
        assertEquals("Hello, world!", template.process())
    }

    @Test
    fun `single value and single condition`() {
        val template = TemplateProcessor("Hello, [[NAME]]![[if:ASK]] How are you?[[endif:ASK]]")
        template.value("NAME", "world")

        template.conditional("ASK", false)
        assertEquals("Hello, world!", template.process())

        template.conditional("ASK", true)
        assertEquals("Hello, world! How are you?", template.process())
    }

    @Test
    fun `optional value as condition`() {
        val template = TemplateProcessor("Hello[[if:NAME]], [[NAME]][[endif:NAME]]!")

        template.optionalValue("NAME", "world")
        assertEquals("Hello, world!", template.process())

        template.optionalValue("NAME", null)
        assertEquals("Hello!", template.process())
    }

    @Test
    fun `optional value if-else`() {
        val template = TemplateProcessor("Hello, [[if:NAME]][[NAME]][[endif:NAME]][[if:!NAME]]unnamed[[endif:!NAME]]!")

        template.optionalValue("NAME", "world")
        assertEquals("Hello, world!", template.process())

        template.optionalValue("NAME", null)
        assertEquals("Hello, unnamed!", template.process())
    }

    @Test
    fun multiline() {
        val template =
            TemplateProcessor(
                """
                Hello, [[NAME]]!
                [[if:ASK]]How are you?
                I hope you are good.[[endif:ASK]]
                """.trimIndent(),
            )
        template.value("NAME", "world")

        template.conditional("ASK", true)
        assertEquals(
            """
            Hello, world!
            How are you?
            I hope you are good.
            """.trimIndent(),
            template.process(),
        )

        template.conditional("ASK", false)
        assertEquals("Hello, world!\n", template.process())
    }

    @Test
    fun `from resource`() {
        val template = TemplateProcessor.fromResourceName("/postrendering/template.txt")
        template.optionalValue("NAME", "world")
        template.conditional("ASK", true)

        assertEquals(
            """
            Hello, world!
            
            How are you?
            I'm good.
            """.trimIndent(),
            template.process(),
        )
    }
}
