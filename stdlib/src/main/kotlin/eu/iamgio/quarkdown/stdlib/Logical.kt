package eu.iamgio.quarkdown.stdlib

import eu.iamgio.quarkdown.function.reflect.annotation.Name
import eu.iamgio.quarkdown.function.value.BooleanValue
import eu.iamgio.quarkdown.function.value.DynamicValue

/**
 * `Logical` stdlib module exporter.
 */
val Logical: Module =
    setOf(
        ::isLower,
        ::isGreater,
        ::isEqual,
        ::not,
    )

/**
 * @param equals whether the comparison should be 'lower or equals' instead
 * @return whether `a < b` (or `<=` if [equals] is `true`)
 */
@Name("islower")
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
@Name("isequal")
fun isEqual(
    a: DynamicValue,
    @Name("to") b: DynamicValue,
) = BooleanValue(a == b || a.unwrappedValue == b.unwrappedValue)

/**
 * @return the negation of [value]
 */
fun not(value: Boolean) = BooleanValue(!value)
