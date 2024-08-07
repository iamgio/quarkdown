package eu.iamgio.quarkdown.ast.quarkdown

import eu.iamgio.quarkdown.ast.Heading
import eu.iamgio.quarkdown.ast.InlineContent
import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.ast.TextNode
import eu.iamgio.quarkdown.ast.id.Identifiable
import eu.iamgio.quarkdown.context.Context
import eu.iamgio.quarkdown.util.findAll
import eu.iamgio.quarkdown.visitor.node.NodeVisitor

/**
 * A summary of the document's structure. Each item links to a section.
 * @param items root sections in the document
 */
data class TableOfContents(val items: List<Item>) : Node {
    override fun <T> accept(visitor: NodeVisitor<T>): T = visitor.visit(this)

    /**
     * An item in the table of contents, usually associated to a section of the document.
     * @param text text of the item
     * @param target element the item links to
     * @param subItems nested items
     */
    data class Item(
        override val text: InlineContent,
        val target: Identifiable,
        val subItems: List<Item> = emptyList(),
    ) : TextNode {
        /**
         * Shorthand constructor for creating an item from a heading.
         * @param heading heading to create the item from
         * @param subItems nested items
         */
        constructor(heading: Heading, subItems: List<Item> = emptyList()) : this(heading.text, heading, subItems)

        override fun <T> accept(visitor: NodeVisitor<T>): T = visitor.visit(this)
    }

    companion object {
        /**
         * Generates a table of contents from a flat sequence of headings, based on their depth.
         *
         * Example:
         *
         * ```
         * H1 ABC
         * H2 DEF
         * H2 GHI
         * H3 JKL
         * H2 MNO
         * H1 PQR
         * ```
         * Should generate:
         * ```
         * - ABC
         *   - DEF
         *   - GHI
         *     - JKL
         *   - MNO
         * - PQR
         * ```
         *
         * @param headings flat sequence of headings
         * @param maxDepth maximum depth of headings to include in the table of contents
         * @return the generated table of contents
         */
        fun generate(
            headings: Sequence<Heading>,
            maxDepth: Int,
        ): TableOfContents {
            /**
             * Helper function to add a heading into the correct place in the hierarchy.
             * @param hierarchy the current hierarchy
             * @param item the item to add
             * @param depth depth of the item to add
             */
            fun addItemToHierarchy(
                hierarchy: List<Item>,
                item: Item,
                depth: Int,
            ): List<Item> {
                if (depth == 1 || hierarchy.isEmpty()) {
                    return hierarchy + item
                }

                val parent = hierarchy.last()
                val newSubItems = addItemToHierarchy(parent.subItems, item, depth - 1)
                return hierarchy.dropLast(1) + parent.copy(subItems = newSubItems)
            }

            // Fold through headings to build the hierarchy via an accumulator.
            val result =
                headings
                    .filter { it.depth <= maxDepth }
                    .fold(emptyList<Item>()) { accumulator, heading ->
                        addItemToHierarchy(accumulator, Item(heading), heading.depth)
                    }

            return TableOfContents(result)
        }

        /**
         * Generates a table of contents from the headings present in [context]'s document.
         * @see generate
         */
        fun generate(
            context: Context,
            maxDepth: Int,
        ): TableOfContents {
            val headings =
                context.attributes.root?.findAll<Heading>()
                    ?: return TableOfContents(emptyList())

            return generate(headings, maxDepth)
        }
    }
}
