package eu.iamgio.quarkdown.stdlib

import eu.iamgio.quarkdown.function.reflect.annotation.Name
import eu.iamgio.quarkdown.function.value.BooleanValue
import eu.iamgio.quarkdown.function.value.DynamicValue
import eu.iamgio.quarkdown.function.value.None
import eu.iamgio.quarkdown.function.value.NoneValue
import eu.iamgio.quarkdown.function.value.wrappedAsValue

/**
 * `Optionality` stdlib module exporter.
 * This module handles `None` values to express optional values.
 */
val Optionality: Module =
    setOf(
        ::none,
        ::isNone,
        ::otherwise,
    )

/**
 * @return a value which represents nothing
 */
fun none() = NoneValue

/**
 * @param value value to check
 * @return whether [value] represents a `None` value
 */
internal fun isNone(value: Any?) = value == null || value is None || value is NoneValue

/**
 * @param value value to check
 * @return whether [value] represents a `None` value
 */
@Name("isnone")
fun isNone(value: DynamicValue): BooleanValue = isNone(value.unwrappedValue).wrappedAsValue()

/**
 * @param value value to check
 * @param fallback value to return if [value] is `None`
 * @return [value] if it is not none, [fallback] otherwise
 * @see isNone
 */
fun otherwise(
    value: DynamicValue,
    fallback: DynamicValue,
): DynamicValue = if (isNone(value.unwrappedValue)) fallback else value
