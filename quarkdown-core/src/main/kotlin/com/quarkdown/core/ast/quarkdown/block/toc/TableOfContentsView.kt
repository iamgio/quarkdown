package com.quarkdown.core.ast.quarkdown.block.toc

import com.quarkdown.core.ast.InlineContent
import com.quarkdown.core.ast.Node
import com.quarkdown.core.ast.attributes.AstAttributes
import com.quarkdown.core.context.toc.TableOfContents
import com.quarkdown.core.util.toPlainText
import com.quarkdown.core.visitor.node.NodeVisitor

/**
 * When this node is rendered, the current table of contents,
 * retrieved from the auto-generated [AstAttributes.tableOfContents], is displayed.
 * @param title title of the table of contents. If `null`, the default localized title is used
 * @param maxDepth maximum depth the table of contents to display.
 *                 For instance, if `maxDepth` is 2, only headings of level 1 and 2 will be displayed
 * @param focusedItem if not `null`, adds focus to the item of the table of contents with the same text content as this value
 */
class TableOfContentsView(
    val title: InlineContent?,
    val maxDepth: Int,
    private val focusedItem: InlineContent? = null,
) : Node {
    override fun <T> accept(visitor: NodeVisitor<T>): T = visitor.visit(this)

    /**
     * @param item table of contents item to compare
     * @return whether the given item of a table of contents should be focused, according to the [focusedItem] property.
     *         Their pure text content (ignoring formatting) is compared.
     */
    fun hasFocus(item: TableOfContents.Item) =
        focusedItem != null &&
            item.text.toPlainText() == focusedItem.toPlainText()
}
