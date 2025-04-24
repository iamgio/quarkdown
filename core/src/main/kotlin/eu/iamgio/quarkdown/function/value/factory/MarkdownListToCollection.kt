package eu.iamgio.quarkdown.function.value.factory

import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.ast.base.TextNode
import eu.iamgio.quarkdown.ast.base.block.list.ListBlock
import eu.iamgio.quarkdown.context.Context
import eu.iamgio.quarkdown.function.value.IterableValue
import eu.iamgio.quarkdown.function.value.NodeValue
import eu.iamgio.quarkdown.function.value.OrderedCollectionValue
import eu.iamgio.quarkdown.function.value.OutputValue
import eu.iamgio.quarkdown.util.toPlainText

/**
 * Helper that converts a Markdown list to an [OrderedCollectionValue].
 * @param list list to convert
 * @param inlineValueMapper function that maps the node of a list item a value.
 * @param nestedValueMapper function that maps a nested list to a value.
 * This is invoked when the entry is in the format:
 * ```
 * - :
 *   - value
 * ```
 * (Note that the `:` character is not mandatory. Any string is valid since it's ignored by the parsing. `:` is the preferred one.)
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

    override fun inlineValue(child: Node) = inlineValueMapper(child)

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
