@file:QModule

package com.quarkdown.stdlib

import com.quarkdown.core.function.reflect.annotation.LikelyBody
import com.quarkdown.core.function.reflect.annotation.LikelyChained
import com.quarkdown.core.function.value.DictionaryValue
import com.quarkdown.core.function.value.DynamicValue
import com.quarkdown.core.function.value.OutputValue
import com.quarkdown.processor.annotation.Name
import com.quarkdown.processor.annotation.QFunction
import com.quarkdown.processor.annotation.QModule

/**
 * Makes the initialization of a dictionary explicit, to avoid ambiguity with collection initialization.
 *
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
 *
 * @param dictionary dictionary to initialize
 * @return the dictionary
 * @wiki dictionary
 */
@QFunction
fun dictionary(
    @LikelyBody dictionary: Map<String, OutputValue<*>>,
): DictionaryValue<*> = DictionaryValue(dictionary.toMutableMap())

/**
 * Gets a value from a dictionary by its key.
 *
 * ```
 * .var {dict}
 *   .dictionary
 *     - a: 1
 *     - b: 2
 *
 * .dict::get {a}
 * ```
 *
 * @param dictionary dictionary to get the value from
 * @param key key to get the value of
 * @param fallback value to return if the key is not present. If unset, defaults to [NOT_FOUND].
 * @return value corresponding to the given key, or [fallback] if the key is not present
 */
@QFunction
@Name("get")
@LikelyChained
fun dictionaryGet(
    dictionary: Map<String, OutputValue<*>>,
    key: String,
    @Name("orelse") fallback: DynamicValue = DynamicValue(NOT_FOUND),
): OutputValue<*> = dictionary[key] ?: fallback
