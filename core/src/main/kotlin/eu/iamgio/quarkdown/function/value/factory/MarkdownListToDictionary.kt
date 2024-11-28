package eu.iamgio.quarkdown.function.value.factory

import eu.iamgio.quarkdown.ast.base.TextNode
import eu.iamgio.quarkdown.ast.base.block.Newline
import eu.iamgio.quarkdown.ast.base.block.list.ListBlock
import eu.iamgio.quarkdown.function.value.DictionaryValue
import eu.iamgio.quarkdown.function.value.OutputValue
import eu.iamgio.quarkdown.util.toPlainText

/**
 * Separator between key and value in a dictionary element.
 */
private const val KEY_VALUE_SEPARATOR = ":"

/**
 * Helper that converts a Markdown list to a [DictionaryValue].
 * @param list list to convert
 * @param inlineValueMapper function that maps a raw string to a value.
 * This is invoked when the entry is in a `- key: value` format.
 * @param nestedValueMapper function that maps a nested list to a value.
 * This is invoked when the entry is in the format:
 * ```
 * - key
 *   - value
 * ```
 * @param T type of values in the dictionary
 * @see DictionaryValue
 * @see ValueFactory.dictionary
 */
class MarkdownListToDictionary<T : OutputValue<*>>(
    private val list: ListBlock,
    private val inlineValueMapper: (String) -> T,
    private val nestedValueMapper: (ListBlock) -> T,
) {
    /**
     * @return [DictionaryValue] representation of the list
     * @throws IllegalRawValueException if the list is not in the correct format
     */
    fun convert(): DictionaryValue<T> =
        mutableMapOf<String, T>().run {
            list.items.asSequence()
                .map { it.children.filterNot { child -> child is Newline } }
                .forEach { children ->
                    // The first child of a list item is usually a Paragraph.
                    // - This
                    val firstChild =
                        children.first() as? TextNode
                            ?: throw IllegalRawValueException(
                                "Dictionary element does not contain a key",
                                children.first(),
                            )

                    // Cases:
                    val (key, value) =
                        when (val secondChild = children.getOrNull(1)) {
                            // Inline value: the item is a key-value pair.
                            // - This: that
                            null -> {
                                val text = firstChild.text.toPlainText()
                                val (key, value) = text.split(KEY_VALUE_SEPARATOR, limit = 2)
                                key to inlineValueMapper(value.trimStart())
                            }

                            // Nested value: the item is a nested dictionary.
                            // - This
                            //     - that: those
                            // The key-value separator at the end of the text is optional.
                            is ListBlock -> {
                                val key = firstChild.text.toPlainText().removeSuffix(KEY_VALUE_SEPARATOR)
                                key to nestedValueMapper(secondChild)
                            }

                            // Not a valid dictionary element.
                            else -> {
                                throw IllegalRawValueException("Unexpected dictionary element", secondChild)
                            }
                        }

                    put(key, value)
                }

            DictionaryValue(this)
        }
}
