package com.quarkdown.core.util

import java.util.concurrent.atomic.AtomicInteger

/**
 * A thread-safe counter that tracks a depth value within nested scopes.
 * @param maxDepth maximum allowed depth. If exceeded, [onOverflow] is invoked
 * @param onOverflow action to perform when the maximum depth is exceeded
 */
class ScopedCounter(
    @PublishedApi internal val maxDepth: Int,
    @PublishedApi internal val onOverflow: () -> Nothing,
) {
    @PublishedApi internal val depth: AtomicInteger = AtomicInteger(0)

    /**
     * The current depth of the counter.
     */
    fun get(): Int = depth.get()

    /**
     * Increments the counter, executes [block], and decrements the counter when [block] completes.
     * If the counter exceeds [maxDepth], [onOverflow] is invoked before executing [block].
     */
    inline fun <T> incrementScoped(block: () -> T): T {
        val current = depth.incrementAndGet()
        if (current > maxDepth) {
            depth.decrementAndGet()
            onOverflow()
        }
        try {
            return block()
        } finally {
            depth.decrementAndGet()
        }
    }
}
