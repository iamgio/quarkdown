package com.quarkdown.core

import com.quarkdown.core.util.ConcurrentCache
import com.quarkdown.core.util.ConcurrentQueue
import com.quarkdown.core.util.mapParallel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame

class ConcurrencyUtilsTest {
    @Test
    fun mapParallelPreservesOrder() {
        val input = (1..100).toList()
        assertEquals(input.map { it * 2 }, input.mapParallel(minItems = 1) { it * 2 })
        assertEquals(input.map { it * 2 }, input.mapParallel(minItems = 1000) { it * 2 })
    }

    @Test
    fun cacheComputesOncePerKey() {
        val cache = ConcurrentCache<String, Any>()
        val first = cache.getOrPut("k") { Any() }
        assertSame(first, cache.getOrPut("k") { Any() })
    }

    @Test
    fun queueIsFifo() {
        val queue = ConcurrentQueue<Int>()
        queue += 1
        queue += 2
        assertEquals(1, queue.poll())
        assertEquals(2, queue.poll())
        assertNull(queue.poll())
        queue += 3
        queue.clear()
        assertNull(queue.poll())
    }
}
