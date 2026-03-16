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
 * @param inlineValueMapper function that maps the node of a list item to a value.
 *        For example, the paragraph node containing `file1.txt` in `- file1.txt`
 * @param nestedValueMapper function that maps a nested list item to a value.
 *        The first argument is the parent node (e.g. the paragraph containing the label `dir1` in `- dir1`),
 *        and the second is the nested [ListBlock] containing its children.
 *        In the compact syntax (`- - value`), both arguments refer to the same [ListBlock] node
 * @see com.quarkdown.core.function.value.factory.ValueFactory.iterable
 */
abstract class MarkdownListToIterable<O, T>(
    list: ListBlock,
    private val inlineValueMapper: (Node) -> T,
    private val nestedValueMapper: (Node, ListBlock) -> T,
) : MarkdownListConverter<O, T, Node>(list) {
    protected val elements = mutableListOf<T>()

    override fun push(element: T) {
        elements += element
    }

    override fun validateChild(firstChild: Node) = firstChild

    override fun inlineValue(child: Node) =
        when (child) {
            // Compact syntax: the parent node is the list itself.
            is ListBlock -> nestedValueMapper(child, child)

            else -> inlineValueMapper(child)
        }

    // Extended syntax.
    override fun nestedValue(
        child: Node,
        list: ListBlock,
    ) = nestedValueMapper(child, list)
}
