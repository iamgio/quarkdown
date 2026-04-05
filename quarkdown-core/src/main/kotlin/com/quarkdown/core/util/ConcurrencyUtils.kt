package com.quarkdown.core.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking

/**
 * Minimum number of items required for parallel execution to be worthwhile.
 * Below this threshold, the overhead of coroutine scheduling exceeds the benefit.
 */
private const val MIN_ITEMS_FOR_PARALLELISM = 4

/**
 * Tracks whether the current thread is already inside a [mapParallel] invocation,
 * preventing nested [runBlocking] calls that would risk thread pool exhaustion.
 */
private val insideParallelBlock = ThreadLocal.withInitial { false }

/**
 * Maps each element of this list using [transform], executing transformations in parallel
 * via coroutines when the list is large enough to benefit from concurrency.
 * Falls back to sequential mapping for small lists where coroutine overhead exceeds benefit.
 *
 * Handles nested invocations safely: if already executing inside a parallel block,
 * the inner call uses [coroutineScope] instead of [runBlocking] to avoid thread pool exhaustion.
 *
 * Results are returned in the same order as the input list.
 * @param transform the transformation to apply to each element
 * @return the list of transformed results, preserving input order
 */
fun <T, R> List<T>.mapParallel(transform: (T) -> R): List<R> {
    if (size < MIN_ITEMS_FOR_PARALLELISM) {
        return map(transform)
    }

    // If already inside a parallel block, delegate to a suspending coroutineScope
    // to participate in the existing dispatcher without blocking a thread.
    if (insideParallelBlock.get()) {
        return runBlocking {
            mapParallelAsync(transform)
        }
    }

    return runBlocking(Dispatchers.Default) {
        mapParallelAsync(transform)
    }
}

/**
 * Suspending implementation of parallel mapping.
 * Launches an [async] coroutine per element and awaits all results in order.
 */
private suspend fun <T, R> List<T>.mapParallelAsync(transform: (T) -> R): List<R> =
    coroutineScope {
        map { element ->
            async {
                val wasInside = insideParallelBlock.get()
                insideParallelBlock.set(true)
                try {
                    transform(element)
                } finally {
                    insideParallelBlock.set(wasInside)
                }
            }
        }.awaitAll()
    }
