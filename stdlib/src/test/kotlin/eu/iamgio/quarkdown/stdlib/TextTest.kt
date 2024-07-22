package eu.iamgio.quarkdown.stdlib

import eu.iamgio.quarkdown.ast.Code
import eu.iamgio.quarkdown.ast.MarkdownContent
import eu.iamgio.quarkdown.ast.Text
import eu.iamgio.quarkdown.context.MutableContext
import eu.iamgio.quarkdown.flavor.quarkdown.QuarkdownFlavor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs

/**
 * [Text] module tests.
 */
class TextTest {
    @Test
    fun `markdown code`() {
        val code =
            code(
                MutableContext(QuarkdownFlavor),
                language = "kotlin",
                showLineNumbers = false,
                MarkdownContent(listOf(Text("fun foo() = 1"))),
            )

        val node = code.unwrappedValue

        assertIs<Code>(node)
        assertEquals("fun foo() = 1", node.content)
        assertEquals("kotlin", node.language)
        assertFalse(node.showLineNumbers)
    }
}
