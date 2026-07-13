@file:QModule

package com.quarkdown.stdlib

import com.quarkdown.core.ast.InlineMarkdownContent
import com.quarkdown.core.function.reflect.annotation.LikelyChained
import com.quarkdown.core.function.value.StringValue
import com.quarkdown.core.function.value.wrappedAsValue
import com.quarkdown.core.util.StringCase
import com.quarkdown.core.util.case
import com.quarkdown.core.util.node.toPlainText
import com.quarkdown.core.util.trimDelimiters
import com.quarkdown.processor.annotation.Name
import com.quarkdown.processor.annotation.QFunction
import com.quarkdown.processor.annotation.QModule

/**
 * Creates a string.
 *
 * If [value] is delimited by `"` characters, they are removed
 * and the wrapped string is not trimmed, as opposed to what usually happens
 * through Quarkdown's parser.
 *
 * Example: `"  Hello, World!  "` -> `  Hello, World!  `
 * @param value string to wrap
 * @return a new string value
 */
@QFunction
fun string(value: String) =
    when {
        value.firstOrNull() == '\"' && value.lastOrNull() == '\"' -> value.trimDelimiters()
        else -> value
    }.wrappedAsValue()

/**
 * Concatenates two strings if a condition is met.
 *
 * ```
 * .concatenate {abc} with:{def} <!-- abcdef -->
 * ```
 *
 * ```
 * .var {condition} {no}
 *
 * .concatenate {abc} with:{def} if:{.condition} <!-- abc -->
 * ```
 *
 * @param a first string
 * @param b second string
 * @param condition if true, concatenates `a` and `b`
 * @return a new string that is the concatenation of `a` and `b` if `condition` is true, `a` otherwise
 */
@QFunction
@LikelyChained
fun concatenate(
    a: String,
    @Name("with") b: String,
    @Name("if") condition: Boolean = true,
): StringValue =
    when {
        condition -> a + b
        else -> a
    }.wrappedAsValue()

/**
 * Converts a string to uppercase.
 *
 * Example: `Hello, World!` -> `HELLO, WORLD!
 *
 * @param string string to convert
 * @return a new uppercase string
 */
@QFunction
@LikelyChained
fun uppercase(string: String) = string.case(StringCase.Upper).wrappedAsValue()

/**
 * Converts a string to lowercase.
 *
 * Example: `Hello, World!` -> `hello, world!`
 *
 * @param string string to convert
 * @return a new lowercase string
 */
@QFunction
@LikelyChained
fun lowercase(string: String) = string.case(StringCase.Lower).wrappedAsValue()

/**
 * Capitalizes the first character of a string.
 *
 * Example: `hello, world!` -> `Hello, world!`
 *
 * @param string string to capitalize
 * @return a new string with the first character capitalized
 */
@QFunction
@LikelyChained
fun capitalize(string: String) = string.case(StringCase.Capitalize).wrappedAsValue()

/**
 * Checks if a string is empty.
 *
 * @param string string to check
 * @return `true` if the string is empty, `false` otherwise
 */
@QFunction
@LikelyChained
@Name("isempty")
fun isEmpty(string: String) = string.isEmpty().wrappedAsValue()

/**
 * Checks if a string is not empty.
 *
 * @param string string to check
 * @return `true` if the string is not empty, `false` otherwise
 * @see isEmpty
 */
@QFunction
@Name("isnotempty")
@LikelyChained
fun isNotEmpty(string: String) = string.isNotEmpty().wrappedAsValue()

/**
 * Checks if a string starts with a given prefix.
 *
 * @param string string to check
 * @param prefix prefix to check for
 * @param ignoreCase whether to ignore case when checking
 * @return `true` if the string starts with the prefix, `false` otherwise
 */
@QFunction
@Name("startswith")
@LikelyChained
fun startsWith(
    string: String,
    prefix: String,
    @Name("ignorecase") ignoreCase: Boolean = false,
) = string.startsWith(prefix, ignoreCase).wrappedAsValue()

/**
 * Converts Markdown content to plain text.
 *
 * Example: `Hello, **world**!` -> `Hello, world!`
 *
 * @param content inline content to convert
 * @return a new string that is the plain text of the content
 */
@QFunction
@Name("plaintext")
fun toPlainText(content: InlineMarkdownContent): StringValue = content.children.toPlainText().wrappedAsValue()
