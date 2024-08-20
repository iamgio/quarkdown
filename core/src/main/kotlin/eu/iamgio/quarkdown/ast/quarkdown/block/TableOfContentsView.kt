package eu.iamgio.quarkdown.ast.quarkdown.block

import eu.iamgio.quarkdown.ast.AstAttributes
import eu.iamgio.quarkdown.ast.InlineContent
import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.visitor.node.NodeVisitor

/**
 * When this node is rendered, the current table of contents,
 * retrieved from the auto-generated [AstAttributes.tableOfContents], is displayed.
 * @param title title of the table of contents. If `null`, the default title is used
 * @param maxDepth maximum depth the table of contents to display.
 *                 For instance, if `maxDepth` is 2, only headings of level 1 and 2 will be displayed
 */
data class TableOfContentsView(
    val title: InlineContent?,
    val maxDepth: Int,
) : Node {
    override fun <T> accept(visitor: NodeVisitor<T>): T = visitor.visit(this)
}
