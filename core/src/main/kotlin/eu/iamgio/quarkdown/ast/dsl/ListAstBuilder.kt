package eu.iamgio.quarkdown.ast.dsl

import eu.iamgio.quarkdown.ast.base.block.list.ListItem
import eu.iamgio.quarkdown.ast.base.block.list.ListItemVariant

/**
 * A builder of list items.
 * @see BlockAstBuilder.orderedList
 * @see BlockAstBuilder.unorderedList
 */
class ListAstBuilder : AstBuilder() {
    /**
     * @see ListItem
     */
    fun listItem(
        vararg variants: ListItemVariant,
        block: BlockAstBuilder.() -> Unit,
    ) = node(ListItem(variants.toList(), buildBlocks(block)))
}
