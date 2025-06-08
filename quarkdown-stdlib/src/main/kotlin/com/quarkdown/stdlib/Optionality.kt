package com.quarkdown.stdlib

import com.quarkdown.core.function.library.loader.Module
import com.quarkdown.core.function.library.loader.moduleOf
import com.quarkdown.core.function.reflect.annotation.LikelyChained
import com.quarkdown.core.function.reflect.annotation.LikelyNamed
import com.quarkdown.core.function.reflect.annotation.Name
import com.quarkdown.core.function.value.BooleanValue
import com.quarkdown.core.function.value.DynamicValue
import com.quarkdown.core.function.value.None
import com.quarkdown.core.function.value.NoneValue
import com.quarkdown.core.function.value.OutputValue
import com.quarkdown.core.function.value.data.Lambda
import com.quarkdown.core.function.value.wrappedAsValue

/**
 * `Optionality` stdlib module exporter.
 * This module handles `None` values to express optional values.
 */
val Optionality: Module =
    moduleOf(
        ::none,
        ::isNone,
        ::otherwise,
        ::ifPresent,
        ::takeIf,
    )

/**
 * @return a value which represents nothing
 * @wiki None
 */
fun none() = NoneValue

/**
 * @param value value to check
 * @return whether [value] represents a `None` value
 * @see [none]
 */
internal fun isNone(value: Any?) = value == null || value is None || value is NoneValue

/**
 * @param value value to check
 * @return whether [value] represents a [none] value
 * @see [none]
 * @wiki None
 */
@Name("isnone")
@LikelyChained
fun isNone(value: DynamicValue): BooleanValue = isNone(value.unwrappedValue).wrappedAsValue()

/**
 * @param value value to check
 * @param fallback value to return if [value] is [none]
 * @return [value] if it is not none, [fallback] otherwise
 * @see isNone
 * @wiki None
 */
@LikelyChained
fun otherwise(
    value: DynamicValue,
    @LikelyNamed fallback: DynamicValue,
): DynamicValue = if (isNone(value.unwrappedValue)) fallback else value

/**
 * Maps [value] to the result of [mapping].
 * @param value value to check
 * @param mapping lambda to execute if [value] is not [none].
 * It should accept one argument, which is [value], and return a value.
 * @return the result of [mapping] executed on [value] if [value] is not [none], [none] otherwise
 * @see isNone
 * @wiki None
 */
@LikelyChained
@Name("ifpresent")
fun ifPresent(
    value: DynamicValue,
    mapping: Lambda,
): OutputValue<*> = if (!isNone(value.unwrappedValue)) mapping.invokeDynamic(value) else NoneValue

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
 * @param condition condition to check, which accepts one argument ([value]) and returns a boolean
 * @return [value] if the result of [condition] is true, [none] otherwise
 * @wiki None
 */
@LikelyChained
@Name("takeif")
fun takeIf(
    value: DynamicValue,
    condition: Lambda,
): OutputValue<*> = if (condition.invoke<Boolean, BooleanValue>(value).unwrappedValue) value else NoneValue
