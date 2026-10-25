@file:QModule

package com.quarkdown.stdlib

import com.quarkdown.core.function.reflect.annotation.LikelyChained
import com.quarkdown.core.function.value.BooleanValue
import com.quarkdown.core.function.value.DynamicValue
import com.quarkdown.processor.annotation.Name
import com.quarkdown.processor.annotation.QFunction
import com.quarkdown.processor.annotation.QModule
import com.quarkdown.stdlib.internal.contains
import com.quarkdown.stdlib.internal.haveEqualContent

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
) = BooleanValue(a == b || haveEqualContent(a.unwrappedValue, b.unwrappedValue))

/**
 * Checks whether a value contains another one:
 * - a collection contains [element] if any of its elements equals it, in the same way as [equals];
 * - a dictionary contains [element] if it is one of its keys;
 * - any other value, such as a string, contains [element] if its text contains the text of [element].
 *
 * ```
 * .var {letters}
 *     - A
 *     - B
 *
 * .letters::contains {B} <!-- true -->
 *
 * .contains {Quarkdown} {ark} <!-- true -->
 * ```
 *
 * @param value collection, dictionary or string to search in
 * @param element element, key or text to search for
 * @return whether [value] contains [element]
 */
@QFunction
@LikelyChained
fun contains(
    value: DynamicValue,
    element: DynamicValue,
): BooleanValue = BooleanValue(value.contains(element))

/**
 * Negates a boolean value.
 * @param value boolean value to negate
 * @return the negation of [value]
 */
@QFunction
@LikelyChained
fun not(value: Boolean) = BooleanValue(!value)

/**
 * Logical conjunction of two boolean values.
 *
 * ```
 * .x::islower than:{10}::and {.x::isgreater than:{0}} <!-- true if 0 < x < 10 -->
 * ```
 *
 * @param a first boolean value
 * @param b second boolean value
 * @return whether both [a] and [b] are `true`
 */
@QFunction
@LikelyChained
fun and(
    a: Boolean,
    b: Boolean,
) = BooleanValue(a && b)

/**
 * Logical disjunction of two boolean values.
 *
 * ```
 * .x::islower than:{0}::or {.x::isgreater than:{10}} <!-- true if x < 0 or x > 10 -->
 * ```
 *
 * @param a first boolean value
 * @param b second boolean value
 * @return whether at least one of [a] and [b] is `true`
 */
@QFunction
@LikelyChained
fun or(
    a: Boolean,
    b: Boolean,
) = BooleanValue(a || b)
