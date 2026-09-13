package com.quarkdown.core.util

import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.decrementAndFetch
import kotlin.concurrent.atomics.incrementAndFetch

/**
 * A thread-safe counter that tracks a depth value within nested scopes.
 * @param maxDepth maximum allowed depth. If exceeded, [onOverflow] is invoked
 * @param onOverflow action to perform when the maximum depth is exceeded
 */
@OptIn(ExperimentalAtomicApi::class)
class ScopedCounter(
    private val maxDepth: Int,
    private val onOverflow: () -> Nothing,
) {
    private val depth = AtomicInt(0)

    /**
     * The current depth of the counter.
     */
    fun get(): Int = depth.load()

    /**
     * Increments the counter, executes [block], and decrements the counter when [block] completes.
     * If the counter exceeds [maxDepth], [onOverflow] is invoked before executing [block].
     */
    fun <T> incrementScoped(block: () -> T): T {
        val current = depth.incrementAndFetch()
        if (current > maxDepth) {
            depth.decrementAndFetch()
            onOverflow()
        }
        try {
            return block()
        } finally {
            depth.decrementAndFetch()
        }
    }
}
