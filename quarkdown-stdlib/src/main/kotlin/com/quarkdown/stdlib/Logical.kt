@file:QModule

package com.quarkdown.stdlib

import com.quarkdown.core.function.reflect.annotation.LikelyChained
import com.quarkdown.core.function.value.BooleanValue
import com.quarkdown.core.function.value.DynamicValue
import com.quarkdown.processor.annotation.Name
import com.quarkdown.processor.annotation.QFunction
import com.quarkdown.processor.annotation.QModule
import com.quarkdown.stdlib.internal.asComparablePlainText

/**
 * @param a first number to compare
 * @param b second number to compare
 * @param equals whether the comparison should be 'lower or equals' instead
 * @return whether `a < b` (or `<=` if [equals] is `true`)
 */
@QFunction
@Name("islower")
@LikelyChained
fun isLower(
    a: Number,
    @Name("than") b: Number,
    @Name("orequals") equals: Boolean = false,
) = BooleanValue(
    if (equals) {
        a.toFloat() <= b.toFloat()
    } else {
        a.toFloat() < b.toFloat()
    },
)

/**
 * @param a first number to compare
 * @param b second number to compare
 * @param equals whether the comparison should be 'greater or equals' instead
 * @return whether `a > b` (or `>=` if [equals] is `true`)
 */
@QFunction
@Name("isgreater")
@LikelyChained
fun isGreater(
    a: Number,
    @Name("than") b: Number,
    @Name("orequals") equals: Boolean = false,
) = BooleanValue(
    if (equals) {
        a.toFloat() >= b.toFloat()
    } else {
        a.toFloat() > b.toFloat()
    },
)

/**
 * Compares two values for equality.
 * @param a first value to compare
 * @param b second value to compare
 * @return whether [a] and [b] have equal content
 */
@QFunction
@Name("equals")
@LikelyChained
fun equals(
    a: DynamicValue,
    @Name("to") b: DynamicValue,
) = BooleanValue(
    a == b ||
        a.unwrappedValue == b.unwrappedValue ||
        asComparablePlainText(a.unwrappedValue) == asComparablePlainText(b.unwrappedValue),
)

/**
 * Negates a boolean value.
 * @param value boolean value to negate
 * @return the negation of [value]
 */
@QFunction
@LikelyChained
fun not(value: Boolean) = BooleanValue(!value)
