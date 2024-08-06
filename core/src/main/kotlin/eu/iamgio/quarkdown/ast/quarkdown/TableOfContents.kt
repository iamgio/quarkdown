package eu.iamgio.quarkdown.ast.quarkdown

import eu.iamgio.quarkdown.ast.Heading
import eu.iamgio.quarkdown.ast.InlineContent
import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.ast.TextNode
import eu.iamgio.quarkdown.context.Context
import eu.iamgio.quarkdown.util.findAll
import eu.iamgio.quarkdown.visitor.node.NodeVisitor

/**
 *
 */
data class TableOfContents(val items: List<Item>) : Node {
    override fun <T> accept(visitor: NodeVisitor<T>): T = visitor.visit(this)

    data class Item(
        override val text: InlineContent,
        val target: Node,
        val subItems: List<Item> = emptyList(),
    ) : TextNode {
        constructor(heading: Heading, subItems: List<Item> = emptyList()) : this(heading.text, heading, subItems)

        override fun <T> accept(visitor: NodeVisitor<T>): T = visitor.visit(this)
    }

    companion object {
        fun generate(
            context: Context,
            headingDepthThreshold: Int,
        ): TableOfContents {
            val headings =
                context.attributes.root?.findAll<Heading>()
                    ?.filter { it.depth <= headingDepthThreshold }
                    ?: return TableOfContents(emptyList())

            // H1 ABC
            // H2 DEF
            // H2 GHI
            // H3 JKL
            // H2 MNO
            // H1 PQR
            // Must generate:
            // - ABC
            //   - DEF
            //   - GHI
            //     - JKL
            //   - MNO
            // - PQR

            TODO()
        }
    }
}
