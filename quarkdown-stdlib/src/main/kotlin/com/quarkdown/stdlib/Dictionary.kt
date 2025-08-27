package com.quarkdown.stdlib

import com.quarkdown.core.function.library.loader.Module
import com.quarkdown.core.function.library.loader.moduleOf
import com.quarkdown.core.function.reflect.annotation.LikelyBody
import com.quarkdown.core.function.reflect.annotation.LikelyChained
import com.quarkdown.core.function.reflect.annotation.Name
import com.quarkdown.core.function.value.DictionaryValue
import com.quarkdown.core.function.value.DynamicValue
import com.quarkdown.core.function.value.OutputValue

/**
 * `Dictionary` stdlib module exporter.
 * This module handles map-like dictionaries.
 */
val Dictionary: Module =
    moduleOf(
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
 * @wiki Dictionary
 */
fun dictionary(
    @LikelyBody dictionary: Map<String, OutputValue<*>>,
): DictionaryValue<*> = DictionaryValue(dictionary.toMutableMap())

/**
 * Gets a value from a dictionary by its key.
 * @param key key to get the value of
 * @param dictionary dictionary to get the value from
 * @param fallback value to return if the key is not present. If unset, `false` is returned.
 * @return value corresponding to the given key, or [NOT_FOUND] if the key is not present
 */
@Name("get")
@LikelyChained
fun dictionaryGet(
    key: String,
    @Name("from") dictionary: Map<String, OutputValue<*>>,
    @Name("orelse") fallback: DynamicValue = DynamicValue(NOT_FOUND),
): OutputValue<*> = dictionary[key] ?: fallback
