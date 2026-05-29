package com.quarkdown.rendering.html

import com.quarkdown.core.ast.AstRoot
import com.quarkdown.core.ast.attributes.id.getId
import com.quarkdown.core.ast.base.block.Heading
import com.quarkdown.core.ast.base.inline.Text
import com.quarkdown.core.context.MutableContext
import com.quarkdown.core.flavor.quarkdown.QuarkdownFlavor
import com.quarkdown.rendering.html.node.QuarkdownHtmlNodeRenderer
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for generation of HTML ids via [HtmlIdentifierProvider].
 */
class HtmlIdentifiersTest {
    private val provider = HtmlIdentifierProvider.of(QuarkdownHtmlNodeRenderer(MutableContext(QuarkdownFlavor)))

    private fun assertIdEquals(
        expected: String,
        headingText: String,
    ) {
        assertEquals(
            expected,
            provider.getId(Heading(1, listOf(Text(headingText)))),
        )
    }

    @Test
    fun `with uppercase`() {
        assertIdEquals("abc", "Abc")
    }

    @Test
    fun `with spaces`() {
        assertIdEquals("abc-def", "Abc Def")
    }

    @Test
    fun `with tabs`() {
        assertIdEquals("abc-def", "Abc\tDef")
    }

    @Test
    fun `with special characters`() {
        assertIdEquals("hello-world", "Hello, World!")
    }

    @Test
    fun `with continuous special characters`() {
        assertIdEquals("hello-world", "Hello,,,   World!!")
    }

    @Test
    fun `with numbers`() {
        assertIdEquals("abc-123", "Abc 123")
    }

    @Test
    fun `with leading numbers`() {
        assertIdEquals("_123abc", "123abc")
    }

    @Test
    fun `with accented letters`() {
        assertIdEquals("abc-déf", "Abc Déf")
    }

    @Test
    fun `with chinese characters`() {
        assertIdEquals("abc-你好", "Abc 你好")
        assertIdEquals("你好-abc", "你好 abc")
    }

    @Test
    fun `duplicate headings get suffixed ids when context root is available`() {
        val first = Heading(1, listOf(Text("Task Requirements")))
        val second = Heading(2, listOf(Text("Task Requirements")))
        val context = MutableContext(QuarkdownFlavor)
        context.attributes.root = AstRoot(listOf(first, second))

        val contextAwareProvider = HtmlIdentifierProvider.of(QuarkdownHtmlNodeRenderer(context), context)

        assertEquals("task-requirements", contextAwareProvider.getId(first))
        assertEquals("task-requirements-1", contextAwareProvider.getId(second))
    }
}
