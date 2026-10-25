package com.quarkdown.stdlib.internal

import com.quarkdown.core.ast.InlineMarkdownContent
import com.quarkdown.core.ast.MarkdownContent
import com.quarkdown.core.function.value.Value
import com.quarkdown.core.util.node.toPlainText

/**
 * Lets Markdown content be compared to its plain-text form
 *
 * Returns `null` for values that have no plain-text representation, so callers can fall back
 * to equality on the underlying value.
 *
 * @return the plain-text projection of [value] if one is defined, `null` otherwise.
 */
internal fun asComparablePlainText(value: Any?): String? =
    when (value) {
        is String -> value
        is Number -> value.toString()
        is InlineMarkdownContent -> value.children.toPlainText()
        is MarkdownContent -> value.children.toPlainText()
        else -> null
    }

/**
 * @return whether the raw values [a] and [b] have equal content, either as they are or in their plain-text form
 */
internal fun haveEqualContent(
    a: Any?,
    b: Any?,
): Boolean = a == b || asComparablePlainText(a)?.let { it == asComparablePlainText(b) } == true

/**
 * The raw content of a value, unwrapped through any number of nested wrappers,
 * or the value itself if it is not wrapped.
 */
private val Any?.rawContent: Any?
    get() = if (this is Value<*>) unwrappedValue.rawContent else this

/**
 * The text to search for a value by: its plain-text form if it has one, its string form otherwise.
 */
private val Value<*>.searchText: String
    get() = asComparablePlainText(unwrappedValue) ?: asString()

/**
 * Checks whether a value contains another one:
 * - a collection contains [element] if any of its elements has equal content, as of [haveEqualContent];
 * - a dictionary contains [element] if it is one of its keys, either as it is or by its text;
 * - any other value, such as a string, contains [element] if its plain text contains the text of [element].
 *
 * @param element element, key or text to search for
 * @return whether this value contains [element]
 */
internal fun Value<*>.contains(element: Value<*>): Boolean =
    when (val content = unwrappedValue) {
        is Value<*> -> content.contains(element)
        is Iterable<*> -> content.any { haveEqualContent(it.rawContent, element.rawContent) }
        is Map<*, *> -> element.unwrappedValue in content || element.searchText in content
        else -> asComparablePlainText(content)?.contains(element.searchText) == true
    }
