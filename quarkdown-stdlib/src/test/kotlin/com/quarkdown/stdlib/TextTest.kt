package com.quarkdown.stdlib

import com.quarkdown.core.ast.base.block.Code
import com.quarkdown.core.ast.base.inline.Text
import com.quarkdown.core.function.value.data.EvaluableString
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
