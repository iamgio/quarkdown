package com.quarkdown.core.util

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.stream.Collectors

/**
 * Default minimum number of items required for parallel execution to be worthwhile.
 * Below this threshold, the overhead of thread scheduling exceeds the benefit.
 */
private const val DEFAULT_MIN_ITEMS_FOR_PARALLELISM = 4

/**
 * Maps each element of this list using [transform], executing transformations in parallel
 * when the list is large enough to benefit from concurrency.
 * Falls back to sequential mapping for small lists where parallelism overhead exceeds benefit.
 *
 * Uses [java.util.stream.Stream.parallel] with the common [java.util.concurrent.ForkJoinPool],
 * which handles nested parallelism via work-stealing without risking deadlocks.
 *
 * Results are returned in the same order as the input list.
 * @param minItems minimum number of items required for parallel execution.
 *                 Lists smaller than this are mapped sequentially
 * @param transform the transformation to apply to each element
 * @return the list of transformed results, preserving input order
 */
fun <T, R> List<T>.mapParallel(
    minItems: Int = DEFAULT_MIN_ITEMS_FOR_PARALLELISM,
    transform: (T) -> R,
): List<R> {
    if (size < minItems) {
        return map(transform)
    }
    return parallelStream().map(transform).collect(Collectors.toList())
}

/**
 * A thread-safe map that associates each key with at most one value:
 * concurrent [getOrPut] calls for the same key always return the same instance,
 * making it safe to store mutable values.
 *
 * On platforms without multithreading, a plain map is a valid implementation.
 */
class ConcurrentCache<K : Any, V : Any> {
    private val map = ConcurrentHashMap<K, V>()

    /**
     * @return the value associated with [key], atomically computing and storing it via [compute] if absent
     */
    fun getOrPut(
        key: K,
        compute: () -> V,
    ): V = map.computeIfAbsent(key) { compute() }
}

/**
 * A thread-safe FIFO queue, safe to fill from concurrent producers.
 *
 * On platforms without multithreading, a plain list is a valid implementation.
 */
class ConcurrentQueue<T : Any> {
    private val queue = ConcurrentLinkedQueue<T>()

    /**
     * Appends [element] to the tail of the queue.
     */
    operator fun plusAssign(element: T) {
        queue += element
    }

    /**
     * Removes and returns the head of the queue, or `null` if the queue is empty.
     */
    fun poll(): T? = queue.poll()

    /**
     * Removes all elements from the queue.
     */
    fun clear() = queue.clear()
}
