package com.quarkdown.core.util.node.conversion.list

import com.quarkdown.core.ast.Node
import com.quarkdown.core.ast.base.block.list.ListBlock

/**
 * Helper that converts a Markdown list to a [List].
 * @param T type of values in the list
 * @param list list to convert
 * @param inlineValueMapper function that maps the node of a list item to a value
 * @param nestedValueMapper function that maps a nested list to a value
 */
class MarkdownListToList<T>(
    list: ListBlock,
    inlineValueMapper: (Node) -> T,
    nestedValueMapper: (ListBlock) -> T,
) : MarkdownListToIterable<List<T>, T>(list, inlineValueMapper, nestedValueMapper) {
    override fun wrap(): List<T> = elements.toList()
}
