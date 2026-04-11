package com.quarkdown.core.util

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
