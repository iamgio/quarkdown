package com.quarkdown.rendering.html

import com.quarkdown.core.ast.attributes.presence.markMathPresence
import com.quarkdown.core.context.MutableContext
import com.quarkdown.core.document.deepCopy
import com.quarkdown.core.flavor.quarkdown.QuarkdownFlavor
import com.quarkdown.rendering.html.post.HtmlPostRenderer
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Tests for system security from HTML code injection and other vulnerabilities.
 */
class HtmlSecurityTest {
    private fun testMacro(
        name: String,
        content: String,
        expectedSnippet: String,
    ) {
        val context = MutableContext(QuarkdownFlavor)

        context.documentInfo = context.documentInfo.deepCopy(texMacros = mapOf(name to content))
        context.attributes.markMathPresence()

        val postRenderer = HtmlPostRenderer(context)
        val result = postRenderer.wrap("")

        assertTrue(expectedSnippet in result, "Expected snippet not found in output: $expectedSnippet")
    }

    @Test
    fun `injection in tex macro content`() {
        testMacro(
            name = "\\hello",
            content = "\", function() {}",
            expectedSnippet = "\"\\\\hello\": \"\\\", function() {}\"",
        )
    }

    @Test
    fun `injection in tex macro name`() {
        testMacro(
            name = """\hello": "", function() {}""",
            content = "\\text {hello}",
            expectedSnippet = "\"\\\\hello\\\": \\\"\\\", function() {}\": \"\\\\text {hello}\"",
        )
    }
}
