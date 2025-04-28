package eu.iamgio.quarkdown.util

/**
 * @param consumeAmount amount of elements to consume
 * @return this sequence, sliced after the first [consumeAmount] elements, as an iterator.
 */
fun <T> Sequence<T>.iterator(consumeAmount: Int): Iterator<T> {
    return drop(consumeAmount).iterator()
}

/**
 * @return the next element if it exists, `null` otherwise
 */
fun <T> Iterator<T>.nextOrNull(): T? = if (hasNext()) next() else null

/**
 * @return [this] sequence where the second element of each pair is not `null`
 */
@Suppress("UNCHECKED_CAST")
fun <A, B> Sequence<Pair<A, B?>>.filterNotNullValues(): Sequence<Pair<A, B>> {
    return this.filter { it.second != null } as Sequence<Pair<A, B>>
}
