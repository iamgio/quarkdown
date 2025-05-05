package com.quarkdown.stdlib

import com.quarkdown.core.function.library.loader.Module
import com.quarkdown.core.function.library.loader.moduleOf
import com.quarkdown.core.function.reflect.annotation.Name
import com.quarkdown.core.function.value.StringValue
import com.quarkdown.core.function.value.wrappedAsValue
import com.quarkdown.core.util.StringCase
import com.quarkdown.core.util.case
import com.quarkdown.core.util.trimDelimiters

/**
 * `String` stdlib module exporter.
 * This module handles string manipulation.
 */
val String: Module =
    moduleOf(
        ::string,
        ::concatenate,
        ::uppercase,
        ::lowercase,
        ::capitalize,
        ::isEmpty,
        ::isNotEmpty,
    )

/**
 * Creates a string.
 * If [value] is delimited by `"` characters, they are removed
 * and the wrapped string is not trimmed, as opposed to what usually happens
 * through Quarkdown's parser.
 * Example: `"  Hello, World!  "` -> `  Hello, World!  `
 * @param value string to wrap
 * @return a new string value
 */
fun string(value: String) =
    when {
        value.firstOrNull() == '\"' && value.lastOrNull() == '\"' -> value.trimDelimiters()
        else -> value
    }.wrappedAsValue()

/**
 * Concatenates two strings if a condition is met.
 * Example: `Hello, ` and `World!` -> `Hello, World!`
 * @param a first string
 * @param b second string
 * @param condition if true, concatenates `a` and `b`
 * @return a new string that is the concatenation of `a` and `b` if `condition` is true, `a` otherwise
 */
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
 * Example: `Hello, World!` -> `HELLO, WORLD!
 * @param string string to convert
 * @return a new uppercase string
 */
fun uppercase(string: String) = string.case(StringCase.Upper).wrappedAsValue()

/**
 * Converts a string to lowercase.
 * Example: `Hello, World!` -> `hello, world!`
 * @param string string to convert
 * @return a new lowercase string
 */
fun lowercase(string: String) = string.case(StringCase.Lower).wrappedAsValue()

/**
 * Capitalizes the first character of a string.
 * Example: `hello, world!` -> `Hello, world!`
 * @param string string to capitalize
 * @return a new string with the first character capitalized
 */
fun capitalize(string: String) = string.case(StringCase.Capitalize).wrappedAsValue()

/**
 * Checks if a string is empty.
 * @param string string to check
 * @return `true` if the string is empty, `false` otherwise
 */
@Name("isempty")
fun isEmpty(string: String) = string.isEmpty().wrappedAsValue()

/**
 * Checks if a string is not empty.
 * @param string string to check
 * @return `true` if the string is not empty, `false` otherwise
 * @see isEmpty
 */
@Name("isnotempty")
fun isNotEmpty(string: String) = string.isNotEmpty().wrappedAsValue()
