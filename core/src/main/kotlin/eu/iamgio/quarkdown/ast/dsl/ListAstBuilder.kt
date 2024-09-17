package eu.iamgio.quarkdown.ast.dsl

import eu.iamgio.quarkdown.ast.base.block.BaseListItem
import eu.iamgio.quarkdown.ast.base.block.TaskListItem

/**
 * A builder of list items.
 * @see BlockAstBuilder.orderedList
 * @see BlockAstBuilder.unorderedList
 */
class ListAstBuilder : AstBuilder() {
    /**
     * @see BaseListItem
     */
    fun listItem(block: BlockAstBuilder.() -> Unit) = node(BaseListItem(children = buildBlocks(block)))

    /**
     * @see TaskListItem
     */
    fun taskListItem(
        checked: Boolean,
        block: BlockAstBuilder.() -> Unit,
    ) = node(TaskListItem(checked, buildBlocks(block)))
}
