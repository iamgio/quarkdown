package com.quarkdown.stdlib

import com.quarkdown.core.function.library.loader.Module
import com.quarkdown.core.function.library.loader.moduleOf
import com.quarkdown.core.function.reflect.annotation.LikelyChained
import com.quarkdown.core.function.reflect.annotation.Name
import com.quarkdown.core.function.value.BooleanValue
import com.quarkdown.core.function.value.DynamicValue

/**
 * `Logical` stdlib module exporter.
 */
val Logical: Module =
    moduleOf(
        ::isLower,
        ::isGreater,
        ::equals,
        ::not,
    )

/**
 * @param equals whether the comparison should be 'lower or equals' instead
 * @return whether `a < b` (or `<=` if [equals] is `true`)
 */
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
 * @param equals whether the comparison should be 'greater or equals' instead
 * @return whether `a > b` (or `>=` if [equals] is `true`)
 */
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
 * @return whether [a] and [b] have equal content
 */
@Name("equals")
@LikelyChained
fun equals(
    a: DynamicValue,
    @Name("to") b: DynamicValue,
) = BooleanValue(a == b || a.unwrappedValue == b.unwrappedValue)

/**
 * @return the negation of [value]
 */
@LikelyChained
fun not(value: Boolean) = BooleanValue(!value)
