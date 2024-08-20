package eu.iamgio.quarkdown.stdlib

import eu.iamgio.quarkdown.ast.base.block.Code
import eu.iamgio.quarkdown.ast.base.inline.Text
import eu.iamgio.quarkdown.function.value.data.EvaluableString
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
                language = "kotlin",
                showLineNumbers = false,
                code = EvaluableString("fun foo() = 1"),
            )

        val node = code.unwrappedValue

        assertIs<Code>(node)
        assertEquals("fun foo() = 1", node.content)
        assertEquals("kotlin", node.language)
        assertFalse(node.showLineNumbers)
    }
}
