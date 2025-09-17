package com.quarkdown.rendering.html

import com.quarkdown.core.context.MutableContext
import com.quarkdown.core.flavor.quarkdown.QuarkdownFlavor
import com.quarkdown.core.template.TemplateProcessor
import com.quarkdown.rendering.html.post.HtmlPostRenderer
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for system security from HTML code injection and other vulnerabilities.
 */
class HtmlSecurityTest {
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

        context.documentInfo =
            context.documentInfo.copy(tex = context.documentInfo.tex.copy(macros = mapOf(name to content)))

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
