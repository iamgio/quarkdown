package eu.iamgio.quarkdown.util

/**
 * @param consumeAmount amount of elements to consume
 * @return this sequence, sliced after the first [consumeAmount] elements, as an iterator.
 */
fun <T> Sequence<T>.iterator(consumeAmount: Int): Iterator<T> {
    return drop(consumeAmount).iterator()
}

fun <T> Iterator<T>.nextOrNull(): T? = if (hasNext()) next() else null
