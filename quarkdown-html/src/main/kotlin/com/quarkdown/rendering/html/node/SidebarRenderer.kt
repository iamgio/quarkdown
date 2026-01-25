package com.quarkdown.rendering.html.node

import com.quarkdown.core.ast.attributes.id.getId
import com.quarkdown.core.ast.quarkdown.block.toc.TableOfContentsView
import com.quarkdown.core.ast.quarkdown.block.toc.convertTableOfContentsToListNode
import com.quarkdown.core.context.Context
import com.quarkdown.rendering.html.HtmlIdentifierProvider

private const val MAX_DEPTH = 3

/**
 * Renderer of the sidebar content, loaded from the document's table of contents,
 * to be injected into the HTML template.
 */
object SidebarRenderer {
    /**
     * Renders the sidebar content.
     * @return rendered sidebar content
     */
    fun render(context: Context): CharSequence {
        val toc = context.attributes.tableOfContents ?: return ""
        val renderer = QuarkdownHtmlNodeRenderer(context)
        val view = TableOfContentsView(title = null, maxDepth = MAX_DEPTH, includeUnnumbered = false)
        val list =
            convertTableOfContentsToListNode(
                view,
                renderer,
                items = toc.items,
                wrapLinksInParagraphs = true,
                linkUrlMapper = { item ->
                    "#" + HtmlIdentifierProvider.of(renderer).getId(item.target)
                },
            )

        return list.accept(renderer)
    }
}
