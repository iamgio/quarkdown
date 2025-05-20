package com.quarkdown.rendering.html

import com.quarkdown.core.ast.attributes.id.getId
import com.quarkdown.core.ast.base.block.Heading
import com.quarkdown.core.ast.base.inline.Strong
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
    @Test
    fun identifiers() {
        val provider = HtmlIdentifierProvider.of(QuarkdownHtmlNodeRenderer(MutableContext(QuarkdownFlavor)))
        assertEquals("abc", provider.getId(Heading(1, listOf(Text("Abc")))))
        assertEquals("abc-def", provider.getId(Heading(1, listOf(Strong(listOf(Text("Abc Def")))))))
        assertEquals("hello-world", provider.getId(Heading(1, listOf(Text("Hello, World!")))))
    }
}
