package eu.iamgio.quarkdown.stdlib

import eu.iamgio.quarkdown.function.reflect.annotation.Name
import eu.iamgio.quarkdown.function.value.DynamicValue
import eu.iamgio.quarkdown.function.value.OutputValue
import eu.iamgio.quarkdown.function.value.PairValue

/**
 * Index of the first element in a collection.
 */
private const val INDEX_STARTS_AT = 1

/**
 * `Collection` stdlib module exporter.
 * This module handles iterable collections.
 */
val Collection: Module =
    setOf(
        ::collectionGet,
        ::collectionFirst,
        ::collectionSecond,
        ::collectionThird,
        ::collectionLast,
        ::pair,
    )

/**
 * @param index index of an element in a collection, starting at 0
 * @return the index of the element in Quarkdown (starting at 1)
 */
private fun quarkdownIndexToKotlin(index: Int) = index - INDEX_STARTS_AT

/**
 * @param index index of an element in Quarkdown (starting at 1)
 * @return the index of the element starting at 0
 */
private fun kotlinIndexToQuarkdown(index: Int) = index + INDEX_STARTS_AT

/**
 * @param index index of the element to get (starting at 0)
 * @param collection collection to get the element from
 * @param fallback value to return if the index is out of bounds
 * @return element at the given index, or [NOT_FOUND] if the index is out of bounds
 */
private fun nativeCollectionGet(
    index: Int,
    collection: Iterable<OutputValue<*>>,
    fallback: OutputValue<*> = NOT_FOUND,
): OutputValue<*> {
    return collection.toList().getOrNull(index) ?: NOT_FOUND
}

/**
 * @param index index of the element to get **(starting at 1)**
 * @param collection collection to get the element from
 * @param fallback value to return if the index is out of bounds. If unset, `false` is returned.
 * @return element at the given index, or [NOT_FOUND] if the index is out of bounds
 */
@Name("getat")
fun collectionGet(
    index: Int,
    @Name("from") collection: Iterable<OutputValue<*>>,
    @Name("orelse") fallback: DynamicValue = DynamicValue(NOT_FOUND),
) = nativeCollectionGet(quarkdownIndexToKotlin(index), collection, fallback)

/**
 * @param collection collection to get the first element from
 * @return first element of the collection, or [NOT_FOUND] if the collection is empty
 */
@Name("first")
fun collectionFirst(
    @Name("from") collection: Iterable<OutputValue<*>>,
) = nativeCollectionGet(0, collection)

/**
 * @param collection collection to get the second element from
 * @return second element of the collection, or [NOT_FOUND] if the collection has less than 2 elements
 */
@Name("second")
fun collectionSecond(
    @Name("from") collection: Iterable<OutputValue<*>>,
) = nativeCollectionGet(1, collection)

/**
 * @param collection collection to get the third element from
 * @return third element of the collection, or [NOT_FOUND] if the collection has less than 3 elements
 */
@Name("third")
fun collectionThird(
    @Name("from") collection: Iterable<OutputValue<*>>,
) = nativeCollectionGet(2, collection)

/**
 * @param collection collection to get the last element from
 * @return last element of the collection, or [NOT_FOUND] if the collection is empty
 */
@Name("last")
fun collectionLast(
    @Name("from") collection: Iterable<OutputValue<*>>,
): OutputValue<*> {
    return collection.toList().lastOrNull() ?: NOT_FOUND
}

/**
 * Creates a new pair.
 * @param first first element of the pair
 * @param second second element of the pair
 * @return a pair of the two elements
 */
fun pair(
    first: DynamicValue,
    second: DynamicValue,
): PairValue<*, *> = PairValue(first to second)
