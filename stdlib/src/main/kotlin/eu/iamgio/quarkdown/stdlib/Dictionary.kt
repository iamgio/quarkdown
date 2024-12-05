package eu.iamgio.quarkdown.stdlib

import eu.iamgio.quarkdown.function.reflect.annotation.Name
import eu.iamgio.quarkdown.function.value.OutputValue

/**
 * `Dictionary` stdlib module exporter.
 * This module handles map-like dictionaries.
 */
val Dictionary: Module =
    setOf(
        ::dictionaryGet,
    )

/**
 * @param key key to get the value of
 * @param dictionary dictionary to get the value from
 * @return value corresponding to the given key, or [NOT_FOUND] if the key is not present
 */
@Name("get")
fun dictionaryGet(
    key: String,
    @Name("from") dictionary: Map<String, OutputValue<*>>,
): OutputValue<*> {
    return dictionary[key] ?: NOT_FOUND
}
