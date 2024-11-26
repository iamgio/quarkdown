package eu.iamgio.quarkdown.function.value.factory

import eu.iamgio.quarkdown.ast.base.TextNode
import eu.iamgio.quarkdown.ast.base.block.Newline
import eu.iamgio.quarkdown.ast.base.block.list.ListBlock
import eu.iamgio.quarkdown.function.value.DictionaryValue
import eu.iamgio.quarkdown.function.value.Value
import eu.iamgio.quarkdown.util.toPlainText

/**
 * Separator between key and value in a dictionary element.
 */
private const val KEY_VALUE_SEPARATOR = ":"

/**
 * Helper that converts a Markdown list to a [DictionaryValue].
 * @param list list to convert
 * @param valueMapper function that maps a string to any value
 * @see DictionaryValue
 * @see ValueFactory.dictionary
 */
class MarkdownListToDictionary(
    private val list: ListBlock,
    private val valueMapper: (String) -> Value<*>,
) {
    /**
     * @return [DictionaryValue] representation of the list
     * @throws IllegalRawValueException if the list is not in the correct format
     */
    fun convert(): DictionaryValue =
        mutableMapOf<String, Value<*>>().run {
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
                    when (val secondChild = children.getOrNull(1)) {
                        // The item is a key-value pair.
                        // - This: that
                        null -> {
                            val text = firstChild.text.toPlainText()
                            val (key, value) = text.split(KEY_VALUE_SEPARATOR, limit = 2)
                            this[key] = valueMapper(value.trimStart())
                        }

                        // The item is a nested dictionary.
                        // - This
                        //     - that: those
                        // The key-value separator at the end of the text is optional.
                        is ListBlock -> {
                            val key = firstChild.text.toPlainText().removeSuffix(KEY_VALUE_SEPARATOR)
                            val value = MarkdownListToDictionary(secondChild, valueMapper).convert()
                            this[key] = value
                        }

                        // Not a valid dictionary element.
                        else -> {
                            throw IllegalRawValueException("Unexpected dictionary element", secondChild)
                        }
                    }
                }

            DictionaryValue(this)
        }
}
