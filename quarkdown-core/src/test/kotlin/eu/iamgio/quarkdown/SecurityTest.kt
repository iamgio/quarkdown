package eu.iamgio.quarkdown

import eu.iamgio.quarkdown.context.MutableContext
import eu.iamgio.quarkdown.flavor.quarkdown.QuarkdownFlavor
import eu.iamgio.quarkdown.rendering.html.HtmlPostRenderer
import eu.iamgio.quarkdown.template.TemplateProcessor
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for system security from code injection and other vulnerabilities.
 */
class SecurityTest {
    private fun texMacrosTemplateProcessor() =
        TemplateProcessor(
            """
            [[for:TEXMACRO]]
            [[TEXMACRO]],
            [[endfor:TEXMACRO]]
            """.trimIndent(),
        )

    private fun testMacro(
        name: String,
        content: String,
        expectedResult: String,
    ) {
        val context = MutableContext(QuarkdownFlavor)
        context.documentInfo.tex.macros[name] = content
        val postRenderer = HtmlPostRenderer(context, ::texMacrosTemplateProcessor)

        assertEquals(
            expectedResult,
            postRenderer.createTemplateProcessor().process().trim(),
        )
    }

    @Test
    fun `injection in tex macro content`() {
        testMacro(
            name = "\\hello",
            content = "\", function() {}",
            expectedResult =
                """
                "\\hello": "\", function() {}",
                """.trimIndent(),
        )
    }

    @Test
    fun `injection in tex macro name`() {
        testMacro(
            name = """\hello": "", function() {}""",
            content = "\\text {hello}",
            expectedResult =
                """
                "\\hello\": \"\", function() {}": "\\text {hello}",
                """.trimIndent(),
        )
    }
}
