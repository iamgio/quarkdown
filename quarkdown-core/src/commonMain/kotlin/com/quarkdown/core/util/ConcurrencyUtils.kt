package com.quarkdown.core.util

/**
 * Default minimum number of items required for parallel execution to be worthwhile.
 * Below this threshold, the overhead of scheduling exceeds the benefit.
 */
internal const val DEFAULT_MIN_ITEMS_FOR_PARALLELISM = 4

/**
 * Maps each element of this list using [transform], executing transformations in parallel
 * when the platform supports it and the list is large enough to benefit from concurrency.
 * Results are returned in the same order as the input list.
 * @param minItems minimum number of items required for parallel execution.
 *                 Lists smaller than this are mapped sequentially
 * @param transform the transformation to apply to each element
 * @return the list of transformed results, preserving input order
 */
expect fun <T, R> List<T>.mapParallel(
    minItems: Int = DEFAULT_MIN_ITEMS_FOR_PARALLELISM,
    transform: (T) -> R,
): List<R>

/**
 * A map that associates each key with at most one value, safe under the platform's concurrency model:
 * concurrent [getOrPut] calls for the same key always return the same instance,
 * making it safe to store mutable values.
 */
expect class ConcurrentCache<K : Any, V : Any>() {
    /**
     * @return the value associated with [key], atomically computing and storing it via [compute] if absent
     */
    fun getOrPut(
        key: K,
        compute: () -> V,
    ): V
}

/**
 * @return an empty mutable map, safe to read and write from concurrent threads under the platform's concurrency model
 */
expect fun <K : Any, V : Any> concurrentMapOf(): MutableMap<K, V>

/**
 * Runs [block] while holding [lock], so that concurrent callers on the same lock run one at a time
 * under the platform's concurrency model.
 * @return the result of [block]
 */
expect inline fun <R> withLock(
    lock: Any,
    block: () -> R,
): R

/**
 * A FIFO queue, safe to fill from concurrent producers under the platform's concurrency model.
 */
expect class ConcurrentQueue<T : Any>() {
    /**
     * Appends [element] to the tail of the queue.
     */
    operator fun plusAssign(element: T)

    /**
     * Removes and returns the head of the queue, or `null` if the queue is empty.
     */
    fun poll(): T?

    /**
     * Removes all elements from the queue.
     */
    fun clear()
}
