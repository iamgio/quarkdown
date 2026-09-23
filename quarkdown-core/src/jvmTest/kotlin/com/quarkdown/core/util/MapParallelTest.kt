package com.quarkdown.core.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class MapParallelTest {
    @Test
    fun `empty list`() {
        val result = emptyList<Int>().mapParallel { it * 2 }
        assertEquals(emptyList(), result)
    }

    @Test
    fun `below threshold is sequential`() {
        val result = listOf(1, 2, 3).mapParallel { it * 2 }
        assertEquals(listOf(2, 4, 6), result)
    }

    @Test
    fun `at threshold is parallel`() {
        val result = listOf(1, 2, 3, 4).mapParallel { it * 2 }
        assertEquals(listOf(2, 4, 6, 8), result)
    }

    @Test
    fun `above threshold preserves order`() {
        val result =
            (1..100).toList().mapParallel { element ->
                Thread.sleep(10 - (element % 10).toLong()) // Varying delay to provoke reordering
                element * 3
            }
        assertEquals((1..100).map { it * 3 }, result)
    }

    @Test
    fun `nested calls do not deadlock`() {
        // Outer list triggers parallelism, each inner list also exceeds the threshold.
        val result =
            (1..8).toList().mapParallel { outer ->
                (1..8).toList().mapParallel { inner ->
                    outer * 10 + inner
                }
            }
        val expected = (1..8).map { outer -> (1..8).map { inner -> outer * 10 + inner } }
        assertEquals(expected, result)
    }

    @Test
    fun `triple nesting does not deadlock`() {
        val result =
            (1..4).toList().mapParallel { a ->
                (1..4).toList().mapParallel { b ->
                    (1..4).toList().mapParallel { c ->
                        a * 100 + b * 10 + c
                    }
                }
            }
        val expected =
            (1..4).map { a ->
                (1..4).map { b ->
                    (1..4).map { c -> a * 100 + b * 10 + c }
                }
            }
        assertEquals(expected, result)
    }

    @Test
    fun `exception propagation`() {
        assertFailsWith<IllegalStateException> {
            (1..10).toList().mapParallel { element ->
                if (element == 5) error("fail")
                element
            }
        }
    }

    @Test
    fun `runs concurrently above threshold`() {
        val threads =
            java.util.concurrent.ConcurrentHashMap
                .newKeySet<String>()
        (1..8).toList().mapParallel {
            threads += Thread.currentThread().name
            Thread.sleep(50)
            it
        }
        assertTrue(threads.size > 1, "Expected multiple threads, got: $threads")
    }
}
