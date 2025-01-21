package eu.iamgio.quarkdown.stdlib

import eu.iamgio.quarkdown.function.reflect.annotation.Name
import eu.iamgio.quarkdown.function.value.BooleanValue
import eu.iamgio.quarkdown.function.value.DynamicValue
import eu.iamgio.quarkdown.function.value.None
import eu.iamgio.quarkdown.function.value.NoneValue
import eu.iamgio.quarkdown.function.value.OutputValue
import eu.iamgio.quarkdown.function.value.data.Lambda
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
        ::takeIf,
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
 * @return whether [value] represents a [none] value
 */
@Name("isnone")
fun isNone(value: DynamicValue): BooleanValue = isNone(value.unwrappedValue).wrappedAsValue()

/**
 * @param value value to check
 * @param fallback value to return if [value] is [none]
 * @return [value] if it is not none, [fallback] otherwise
 * @see isNone
 */
fun otherwise(
    value: DynamicValue,
    fallback: DynamicValue,
): DynamicValue = if (isNone(value.unwrappedValue)) fallback else value

/**
 * Keeps [value] if [condition] is true, otherwise returns [none].
 *
 * Note: this function is usually inlined. When inlining lambda arguments, an explicit `@lambda` annotation is required:
 * ```
 * .takeif {5} {@lambda x: .iseven {.x}}
 * ```
 *
 * This function is particularly useful when chained, for example:
 * ```
 * .sum {2} {3}::takeif {@lambda x: .iseven {.x}}::otherwise {0}
 * ```
 *
 * @param value value to check
 * @param condition condition to check
 * @return [value] if [condition] is true, [none] otherwise
 */
@Name("takeif")
fun takeIf(
    value: DynamicValue,
    condition: Lambda,
): OutputValue<*> = if (condition.invoke<Boolean, BooleanValue>(value).unwrappedValue) value else NoneValue
