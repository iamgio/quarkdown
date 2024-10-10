package eu.iamgio.quarkdown.stdlib

import eu.iamgio.quarkdown.function.value.wrappedAsValue

/**
 * `String` stdlib module exporter.
 * This module handles string manipulation.
 */
val String: Module =
    setOf(
        ::uppercase,
        ::lowercase,
        ::capitalize,
    )

/**
 * Converts a string to uppercase.
 * Example: `Hello, World!` -> `HELLO, WORLD!
 * @param string string to convert
 * @return a new uppercase string
 */
fun uppercase(string: String) = string.uppercase().wrappedAsValue()

/**
 * Converts a string to lowercase.
 * Example: `Hello, World!` -> `hello, world!`
 * @param string string to convert
 * @return a new lowercase string
 */
fun lowercase(string: String) = string.lowercase().wrappedAsValue()

/**
 * Capitalizes the first character of a string.
 * Example: `hello, world!` -> `Hello, world!`
 * @param string string to capitalize
 * @return a new string with the first character capitalized
 */
fun capitalize(string: String) = string.replaceFirstChar(Char::titlecase).wrappedAsValue()
