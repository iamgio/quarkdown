package com.quarkdown.core.function.value.factory

import com.quarkdown.core.ast.Node
import com.quarkdown.core.ast.base.block.Newline
import com.quarkdown.core.ast.base.block.list.ListBlock
import com.quarkdown.core.function.value.DictionaryValue
import com.quarkdown.core.function.value.OutputValue

/**
 * Helper that converts a Markdown list to a [Value] of type [V].
 * @param list list to convert
 * @param V type of value to convert to
 * @param E type of elements that compose the output value
 * @param N type of nodes, children of the list, that can be handled
 * @see DictionaryValue
 * @see ValueFactory.dictionary
 */
abstract class MarkdownListToValue<V : OutputValue<*>, E, N : Node>(
    private val list: ListBlock,
) {
    /**
     * Pushes an [element] to the internal value.
     */
    protected abstract fun push(element: E)

    /**
     * Validates the first child of a list item and converts it to a subclass of type [N].
     * @param firstChild the first child of a list item
     * @return the validated child, converted to a [Node] subclass of type [N]
     * @throws IllegalRawValueException if the child is not valid
     */
    protected abstract fun validateChild(firstChild: Node): N

    /**
     * Converts the inline child of a list item to a pushable element.
     * "Inline" means the list item does not contain a nested list, for example:
     * ```
     * - Inline 1
     * - Inline 2
     * - Nested
     *   - Inline 3
     * ```
     * @param child the first child of a list item. In the previous example, it would be a paragraph which contains "Inline 1", "Inline 2" or "Inline 3".
     * @return the element to push
     */
    protected abstract fun inlineValue(child: N): E

    /**
     * Converts the nested child of a list item to a pushable element.
     * ```
     * - Nested
     *   - A
     *   - B
     *   - C
     * ```
     * @param child the first child of a list item. In the previous example, it would be a paragraph which contains "Nested".
     * @param list the Markdown nested list. In the previous example, it would be a list which contains "A", "B" and "C".
     * @return the element to push
     */
    protected abstract fun nestedValue(
        child: N,
        list: ListBlock,
    ): E

    /**
     * Wraps the pushed elements into a value of type [V].
     * @return the wrapped value
     */
    protected abstract fun wrap(): V

    /**
     * @return [list] converted to a value of type [V]
     * @throws IllegalRawValueException if the list is not in the correct format
     */
    fun convert(): V {
        list.items
            .asSequence()
            .map { it.children.filterNot { child -> child is Newline } }
            .forEach { children ->
                val firstChild: N = validateChild(children.first())
                when (val secondChild = children.getOrNull(1)) {
                    null -> push(inlineValue(firstChild))
                    is ListBlock -> push(nestedValue(firstChild, secondChild))
                    else -> throw IllegalRawValueException("Unexpected element", secondChild)
                }
            }
        return wrap()
    }
}
