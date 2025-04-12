package eu.iamgio.quarkdown.stdlib

import eu.iamgio.quarkdown.function.reflect.annotation.Name
import eu.iamgio.quarkdown.function.value.DictionaryValue
import eu.iamgio.quarkdown.function.value.DynamicValue
import eu.iamgio.quarkdown.function.value.OutputValue

/**
 * `Dictionary` stdlib module exporter.
 * This module handles map-like dictionaries.
 */
val Dictionary: Module =
    setOf(
        ::dictionary,
        ::dictionaryGet,
    )

/**
 * Makes the initialization of a dictionary explicit, to avoid ambiguity with collection initialization.
 * ```
 * .var {dict}
 *   .dictionary
 *     - a:
 *       - aa: 1
 *       - ab: 2
 *     - b:
 *       - ba: 3
 *       - bb: 4
 *
 * .foreach {.dict}
 *   It would not iterate key-value pairs properly without the explicit `.dictionary` call.
 * ```
 * @param dictionary dictionary to initialize
 * @return the dictionary
 */
fun dictionary(dictionary: Map<String, OutputValue<*>>): DictionaryValue<*> = DictionaryValue(dictionary.toMutableMap())

/**
 * @param key key to get the value of
 * @param dictionary dictionary to get the value from
 * @param fallback value to return if the key is not present. If unset, `false` is returned.
 * @return value corresponding to the given key, or [NOT_FOUND] if the key is not present
 */
@Name("get")
fun dictionaryGet(
    key: String,
    @Name("from") dictionary: Map<String, OutputValue<*>>,
    @Name("orelse") fallback: DynamicValue = DynamicValue(NOT_FOUND),
): OutputValue<*> {
    return dictionary[key] ?: fallback
}
