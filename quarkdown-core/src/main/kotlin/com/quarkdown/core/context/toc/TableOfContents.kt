package com.quarkdown.core.context.toc

import com.quarkdown.core.ast.InlineContent
import com.quarkdown.core.ast.attributes.id.Identifiable
import com.quarkdown.core.ast.base.block.Heading

/**
 * A summary of the document's structure. Each item links to a section.
 * @param items root sections in the document
 */
data class TableOfContents(
    val items: List<Item>,
) {
    /**
     * An item in the table of contents, usually associated to a section of the document.
     * @param text text of the item
     * @param target element the item links to
     * @param depth depth of the item.
     *              This does not necessarily correspond to the depth of this item in the stack of items,
     *              but rather represents the importance of the item.
     * @param isDecorative whether the item is decorative, and should be included in the ToC at discretion of the renderer
     * @param subItems nested items
     */
    data class Item(
        val text: InlineContent,
        val target: Identifiable,
        val depth: Int,
        val isDecorative: Boolean,
        val subItems: List<Item> = emptyList(),
    ) {
        /**
         * Shorthand constructor for creating an item from a heading.
         * @param heading heading to create the item from
         * @param subItems nested items
         */
        constructor(heading: Heading, subItems: List<Item> = emptyList()) : this(
            heading.text,
            heading,
            heading.depth,
            heading.isDecorative,
            subItems,
        )
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
         * @return the generated table of contents
         */
        fun generate(headings: Sequence<Heading>): TableOfContents {
            /**
             * Helper function to add a heading into the correct place in the hierarchy.
             * @param hierarchy the current hierarchy
             * @param item the item to add
             */
            fun addItemToHierarchy(
                hierarchy: List<Item>,
                item: Item,
            ): List<Item> {
                if (hierarchy.isEmpty()) {
                    return hierarchy + item
                }

                val parent = hierarchy.last()

                // If the new item's depth is less than or equal to the parent's depth,
                // it should be a sibling, not a child.
                if (item.depth <= parent.depth) {
                    return hierarchy + item
                }

                // Otherwise, add as a descendant of the parent.
                val newSubItems = addItemToHierarchy(parent.subItems, item)
                return hierarchy.dropLast(1) + parent.copy(subItems = newSubItems)
            }

            // Fold through headings to build the hierarchy via an accumulator.
            val result =
                headings
                    .fold(emptyList<Item>()) { accumulator, heading ->
                        addItemToHierarchy(accumulator, Item(heading))
                    }

            return TableOfContents(result)
        }
    }
}
