package com.quarkdown.rendering.html

import com.quarkdown.core.ast.base.block.Heading
import com.quarkdown.core.ast.dsl.buildInline
import com.quarkdown.core.context.MutableContext
import com.quarkdown.core.context.toc.TableOfContents
import com.quarkdown.core.flavor.quarkdown.QuarkdownFlavor
import com.quarkdown.rendering.html.node.SidebarRenderer
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for [SidebarRenderer].
 */
class SidebarRendererTest {
    private fun entry(
        depth: Int,
        title: String,
        vararg subItems: TableOfContents.Item = emptyArray(),
        customId: String? = null,
    ) = TableOfContents.Item(
        Heading(depth, buildInline { text(title) }, customId = customId),
        subItems.toList(),
    )

    @Test
    fun `single entry`() {
        val context = MutableContext(QuarkdownFlavor)
        context.attributes.tableOfContents =
            TableOfContents(
                listOf(entry(1, "Title")),
            )
        val sidebar = SidebarRenderer.render(context)
        assertEquals(
            "<ol><li data-target-id=\"title\" data-depth=\"1\"><a href=\"#title\">Title</a></li></ol>",
            sidebar,
        )
    }

    @Test
    fun `custom id`() {
        val context = MutableContext(QuarkdownFlavor)
        context.attributes.tableOfContents =
            TableOfContents(
                listOf(entry(1, "Title", customId = "custom-id")),
            )
        val sidebar = SidebarRenderer.render(context)
        assertEquals(
            "<ol><li data-target-id=\"custom-id\" data-depth=\"1\"><a href=\"#custom-id\">Title</a></li></ol>",
            sidebar,
        )
    }

    @Test
    fun `multiple entries, same depth`() {
        val context = MutableContext(QuarkdownFlavor)
        context.attributes.tableOfContents =
            TableOfContents(
                listOf(
                    entry(1, "Title 1"),
                    entry(1, "Title 2"),
                ),
            )
        val sidebar = SidebarRenderer.render(context)
        assertEquals(
            "<ol><li data-target-id=\"title-1\" data-depth=\"1\"><a href=\"#title-1\">Title 1</a></li>" +
                "<li data-target-id=\"title-2\" data-depth=\"1\"><a href=\"#title-2\">Title 2</a></li></ol>",
            sidebar,
        )
    }

    @Test
    fun `nested entries`() {
        val context = MutableContext(QuarkdownFlavor)
        context.attributes.tableOfContents =
            TableOfContents(
                listOf(
                    entry(
                        1,
                        "Title 1",
                        entry(2, "Subtitle 1.1"),
                        entry(2, "Subtitle 1.2"),
                    ),
                    entry(1, "Title 2", entry(2, "Subtitle 2.1")),
                ),
            )
        val sidebar = SidebarRenderer.render(context)
        assertEquals(
            "<ol>" +
                "<li data-target-id=\"title-1\" data-depth=\"1\"><a href=\"#title-1\">Title 1</a>" +
                "<ol>" +
                "<li data-target-id=\"subtitle-11\" data-depth=\"2\"><a href=\"#subtitle-11\">Subtitle 1.1</a></li>" +
                "<li data-target-id=\"subtitle-12\" data-depth=\"2\"><a href=\"#subtitle-12\">Subtitle 1.2</a></li>" +
                "</ol>" +
                "</li>" +
                "<li data-target-id=\"title-2\" data-depth=\"1\"><a href=\"#title-2\">Title 2</a>" +
                "<ol>" +
                "<li data-target-id=\"subtitle-21\" data-depth=\"2\"><a href=\"#subtitle-21\">Subtitle 2.1</a></li>" +
                "</ol>" +
                "</li>" +
                "</ol>",
            sidebar,
        )
    }
}
