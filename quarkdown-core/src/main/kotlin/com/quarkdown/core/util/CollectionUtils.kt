package com.quarkdown.core.util

/**
 * @param consumeAmount amount of elements to consume
 * @return this sequence, sliced after the first [consumeAmount] elements, as an iterator.
 */
fun <T> Sequence<T>.iterator(consumeAmount: Int): Iterator<T> = drop(consumeAmount).iterator()

/**
 * @return the next element if it exists, `null` otherwise
 */
fun <T> Iterator<T>.nextOrNull(): T? = if (hasNext()) next() else null

/**
 * @return [this] sequence where the second element of each pair is not `null`
 */
@Suppress("UNCHECKED_CAST")
fun <A, B> Sequence<Pair<A, B?>>.filterNotNullValues(): Sequence<Pair<A, B>> =
    this
        .filter { it.second != null }
        .map { it.first to it.second!! }

/**
 * @return [this] sequence where both elements of each pair are not `null`
 */
@Suppress("UNCHECKED_CAST")
fun <A, B> Sequence<Pair<A?, B?>>.filterNotNullEntries(): Sequence<Pair<A, B>> =
    this
        .filter { it.first != null && it.second != null }
        .map { it.first!! to it.second!! }
