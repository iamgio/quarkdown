package com.quarkdown.core.util.node.conversion.list

import com.quarkdown.core.ast.Node
import com.quarkdown.core.ast.base.block.list.ListBlock

/**
 * Abstract helper that converts a Markdown list to a flat (non-keyed) output of type [O],
 * by mapping each list item to an element of type [T].
 *
 * Inline and nested list syntaxes are both supported:
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
 * @param O type of the final converted output
 * @param T type of individual element values in the list
 * @param list list to convert
 * @param inlineValueMapper function that maps the node of a list item to a value
 * @param nestedValueMapper function that maps a nested list to a value
 * @see com.quarkdown.core.function.value.factory.ValueFactory.iterable
 */
abstract class MarkdownListToIterable<O, T>(
    list: ListBlock,
    private val inlineValueMapper: (Node) -> T,
    private val nestedValueMapper: (ListBlock) -> T,
) : MarkdownListConverter<O, T, Node>(list) {
    protected val elements = mutableListOf<T>()

    override fun push(element: T) {
        elements += element
    }

    override fun validateChild(firstChild: Node) = firstChild

    override fun inlineValue(child: Node) =
        when (child) {
            is ListBlock -> nestedValueMapper(child)

            // Compact syntax.
            else -> inlineValueMapper(child)
        }

    // Extended syntax.
    override fun nestedValue(
        child: Node,
        list: ListBlock,
    ) = nestedValueMapper(list) // The child node is ignored.
}
