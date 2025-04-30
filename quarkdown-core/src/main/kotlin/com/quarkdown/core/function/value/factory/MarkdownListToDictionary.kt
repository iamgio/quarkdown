package com.quarkdown.core.function.value.factory

import com.quarkdown.core.ast.Node
import com.quarkdown.core.ast.base.TextNode
import com.quarkdown.core.ast.base.block.list.ListBlock
import com.quarkdown.core.context.Context
import com.quarkdown.core.function.value.DictionaryValue
import com.quarkdown.core.function.value.OutputValue
import com.quarkdown.core.function.value.wrappedAsValue
import com.quarkdown.core.util.toPlainText

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
 * @param nothingValueMapper function that maps an empty value to a value.
 * This is invoked when the entry is in the format:
 * ```
 * - key
 * ```
 * @param T type of values in the dictionary
 * @see DictionaryValue
 * @see ValueFactory.dictionary
 */
class MarkdownListToDictionary<T : OutputValue<*>>(
    list: ListBlock,
    private val inlineValueMapper: (String) -> T,
    private val nestedValueMapper: (ListBlock) -> T,
    private val nothingValueMapper: () -> T,
) : MarkdownListToValue<DictionaryValue<T>, Pair<String, T>, TextNode>(list) {
    private val map = mutableMapOf<String, T>()

    override fun push(element: Pair<String, T>) {
        map[element.first] = element.second
    }

    override fun validateChild(firstChild: Node) =
        firstChild as? TextNode
            ?: throw IllegalRawValueException(
                "Dictionary element does not contain a key",
                firstChild,
            )

    override fun inlineValue(child: TextNode): Pair<String, T> {
        val text = child.text.toPlainText()
        // A key-value pair.
        // - This: that
        val parts = text.split(KEY_VALUE_SEPARATOR, limit = 2)
        val key = parts.first()
        return key to
            when {
                parts.size > 1 -> inlineValueMapper(parts[1].trimStart())
                else -> nothingValueMapper()
            }
    }

    override fun nestedValue(
        child: TextNode,
        list: ListBlock,
    ): Pair<String, T> {
        // Nested dictionary.
        // - This
        //     - that: those
        // The key-value separator at the end of the text is optional.
        val key = child.text.toPlainText().removeSuffix(KEY_VALUE_SEPARATOR)
        return key to nestedValueMapper(list)
    }

    override fun wrap() = DictionaryValue(map)

    companion object {
        /**
         * [MarkdownListToDictionary] factory via a [ValueFactory].
         * @param list list to convert
         * @param context context to use for the conversion
         */
        fun viaValueFactory(
            list: ListBlock,
            context: Context,
        ): MarkdownListToDictionary<*> =
            MarkdownListToDictionary(
                list,
                // Node values are currently unsupported as dictionary values.
                // Here we give back the raw string as a fallback in case a node is met.
                inlineValueMapper = { ValueFactory.eval(it, context, fallback = { it.wrappedAsValue() }) },
                nestedValueMapper = { viaValueFactory(it, context).convert() },
                nothingValueMapper = { DictionaryValue(mutableMapOf()) },
            )
    }
}
