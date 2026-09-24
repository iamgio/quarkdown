package com.quarkdown.core.util.node.conversion.list

import com.quarkdown.core.ast.Node
import com.quarkdown.core.ast.base.TextNode
import com.quarkdown.core.ast.base.block.list.ListBlock
import com.quarkdown.core.context.Context
import com.quarkdown.core.function.value.DynamicValue
import com.quarkdown.core.function.value.NodeValue
import com.quarkdown.core.function.value.OrderedCollectionValue
import com.quarkdown.core.function.value.OutputValue

/**
 * Helper that converts a Markdown list to an [OrderedCollectionValue].
 * @param list list to convert
 * @param inlineValueMapper function that maps the node of a list item to a value
 * @param nestedValueMapper function that maps a nested list item to a value.
 *        The first argument is the parent node, and the second is the nested [ListBlock]
 * @param T type of values in the collection
 * @see OrderedCollectionValue
 * @see com.quarkdown.core.function.value.factory.ValueFactory.iterable
 */
class MarkdownListToCollectionValue<T : OutputValue<*>>(
    list: ListBlock,
    inlineValueMapper: (Node, rawContent: String?) -> T,
    nestedValueMapper: (Node, ListBlock) -> T,
) : MarkdownListToIterable<OrderedCollectionValue<T>, T>(list, inlineValueMapper, nestedValueMapper) {
    override fun wrap(): OrderedCollectionValue<T> = OrderedCollectionValue(elements.toList())

    companion object {
        /**
         * [MarkdownListToCollectionValue] factory via a [com.quarkdown.core.function.value.factory.ValueFactory].
         * @param list list to convert
         * @param context context to use for the conversion
         */
        fun viaValueFactory(
            list: ListBlock,
            context: Context,
        ): MarkdownListToCollectionValue<*> =
            MarkdownListToCollectionValue(
                list,
                inlineValueMapper = { node, rawContent ->
                    when {
                        node is TextNode && rawContent != null -> DynamicValue(rawContent, context)
                        else -> NodeValue(node)
                    }
                },
                nestedValueMapper = { _, list -> viaValueFactory(list, context).convert() },
            )
    }
}
