package com.quarkdown.core.function.value.factory

import com.quarkdown.core.ast.Node
import com.quarkdown.core.ast.base.TextNode
import com.quarkdown.core.ast.base.block.list.ListBlock
import com.quarkdown.core.context.Context
import com.quarkdown.core.function.value.IterableValue
import com.quarkdown.core.function.value.NodeValue
import com.quarkdown.core.function.value.OrderedCollectionValue
import com.quarkdown.core.function.value.OutputValue
import com.quarkdown.core.util.toPlainText

/**
 * Helper that converts a Markdown list to an [OrderedCollectionValue].
 * @param list list to convert
 * @param inlineValueMapper function that maps the node of a list item a value.
 * @param nestedValueMapper function that maps a nested list to a value.
 * This is invoked when the entry is in any of these formats:
 *
 * - Extended:
 *   ```
 *   - :
 *     - value
 *   ```
 *   (Note that the `:` character is not mandatory. Any string is valid since it's ignored by the parsing. `:` is the preferred one.)
 *
 * - Compact:
 *   ```
 *   - - value
 *   ```
 * @param T type of values in the list
 * @see OrderedCollectionValue
 * @see ValueFactory.iterable
 */
class MarkdownListToCollection<T : OutputValue<*>>(
    list: ListBlock,
    private val inlineValueMapper: (Node) -> T,
    private val nestedValueMapper: (ListBlock) -> T,
) : MarkdownListToValue<IterableValue<T>, T, Node>(list) {
    private val list = mutableListOf<T>()

    override fun push(element: T) {
        list += element
    }

    override fun validateChild(firstChild: Node) = firstChild

    override fun inlineValue(child: Node) =
        when (child) {
            is ListBlock -> nestedValueMapper(child) // Compact syntax.
            else -> inlineValueMapper(child)
        }

    // Extended syntax.
    override fun nestedValue(
        child: Node,
        list: ListBlock,
    ) = nestedValueMapper(list) // The child node is ignored.

    override fun wrap() = OrderedCollectionValue(list)

    companion object {
        /**
         * [MarkdownListToCollection] factory via a [ValueFactory].
         * @param list list to convert
         * @param context context to use for the conversion
         */
        fun viaValueFactory(
            list: ListBlock,
            context: Context,
        ): MarkdownListToCollection<*> =
            MarkdownListToCollection(
                list,
                inlineValueMapper = {
                    when (it) {
                        is TextNode -> ValueFactory.eval(it.text.toPlainText(), context)
                        else -> NodeValue(it)
                    }
                },
                nestedValueMapper = { viaValueFactory(it, context).convert() },
            )
    }
}
