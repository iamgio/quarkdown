package com.quarkdown.core.util

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.stream.Collectors

/**
 * Uses [java.util.stream.Stream.parallel] with the common [java.util.concurrent.ForkJoinPool],
 * which handles nested parallelism via work-stealing without risking deadlocks.
 */
actual fun <T, R> List<T>.mapParallel(
    minItems: Int,
    transform: (T) -> R,
): List<R> {
    if (size < minItems) {
        return map(transform)
    }
    return parallelStream().map(transform).collect(Collectors.toList())
}

actual class ConcurrentCache<K : Any, V : Any> {
    private val map = ConcurrentHashMap<K, V>()

    actual fun getOrPut(
        key: K,
        compute: () -> V,
    ): V = map.computeIfAbsent(key) { compute() }
}

actual class ConcurrentQueue<T : Any> {
    private val queue = ConcurrentLinkedQueue<T>()

    actual operator fun plusAssign(element: T) {
        queue += element
    }

    actual fun poll(): T? = queue.poll()

    actual fun clear() = queue.clear()
}
