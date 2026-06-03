package com.quarkdown.rendering.html

import com.quarkdown.core.ast.attributes.id.getId
import com.quarkdown.core.ast.attributes.id.setIdentifierDeduplicationIndex
import com.quarkdown.core.ast.base.block.Heading
import com.quarkdown.core.ast.base.inline.Text
import com.quarkdown.core.ast.dsl.buildInline
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
    fun `disambiguates colliding heading identifiers via deduplication index`() {
        val context = MutableContext(QuarkdownFlavor)
        val provider = HtmlIdentifierProvider.of(QuarkdownHtmlNodeRenderer(context), context)

        val first = Heading(1, buildInline { text("Examples") })
        val second =
            Heading(
                1,
                buildInline {
                    text("Exam")
                    strong { text("ples") }
                },
            )
        val third = Heading(1, buildInline { text("Examples") })

        // Simulate the population performed by HeadingIdentifierDeduplicationHook during tree traversal.
        second.setIdentifierDeduplicationIndex(context, 1)
        third.setIdentifierDeduplicationIndex(context, 2)

        assertEquals("examples", provider.getId(first))
        assertEquals("examples-2", provider.getId(second))
        assertEquals("examples-3", provider.getId(third))
    }

    @Test
    fun `disambiguates colliding custom identifiers via deduplication index`() {
        val context = MutableContext(QuarkdownFlavor)
        val provider = HtmlIdentifierProvider.of(QuarkdownHtmlNodeRenderer(context), context)

        val first = Heading(1, listOf(Text("Foo")), customId = "shared")
        val second = Heading(1, listOf(Text("Bar")), customId = "shared")

        second.setIdentifierDeduplicationIndex(context, 1)

        assertEquals("shared", provider.getId(first))
        assertEquals("shared-2", provider.getId(second))
    }
}
