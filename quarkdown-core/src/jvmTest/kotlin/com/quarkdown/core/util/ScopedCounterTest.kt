package com.quarkdown.core.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ScopedCounterTest {
    @Test
    fun `increments and decrements around block`() {
        val counter = ScopedCounter(maxDepth = 10) { error("overflow") }

        assertEquals(0, counter.get())
        counter.incrementScoped {
            assertEquals(1, counter.get())
            counter.incrementScoped {
                assertEquals(2, counter.get())
            }
            assertEquals(1, counter.get())
        }
        assertEquals(0, counter.get())
    }

    @Test
    fun `resets on exception`() {
        val counter = ScopedCounter(maxDepth = 10) { error("overflow") }

        runCatching {
            counter.incrementScoped {
                counter.incrementScoped {
                    throw RuntimeException("inner failure")
                }
            }
        }

        assertEquals(0, counter.get())
    }

    @Test
    fun `overflows at max depth`() {
        val counter = ScopedCounter(maxDepth = 3) { throw IllegalStateException("overflow") }

        assertFailsWith<IllegalStateException> {
            counter.incrementScoped {
                counter.incrementScoped {
                    counter.incrementScoped {
                        counter.incrementScoped {
                            // This 4th level should trigger overflow.
                        }
                    }
                }
            }
        }
    }

    @Test
    fun `resets after overflow`() {
        val counter = ScopedCounter(maxDepth = 2) { throw IllegalStateException("overflow") }

        assertFailsWith<IllegalStateException> {
            counter.incrementScoped {
                counter.incrementScoped {
                    counter.incrementScoped {}
                }
            }
        }

        assertEquals(0, counter.get())

        // Counter should be usable again after overflow.
        counter.incrementScoped {
            counter.incrementScoped {}
        }
    }

    @Test
    fun `sequential calls do not accumulate`() {
        val counter = ScopedCounter(maxDepth = 1) { throw IllegalStateException("overflow") }

        // Each call enters and exits — depth never exceeds 1.
        repeat(100) {
            counter.incrementScoped {}
        }
        assertEquals(0, counter.get())
    }

    @Test
    fun `returns block result`() {
        val counter = ScopedCounter(maxDepth = 10) { error("overflow") }
        val result = counter.incrementScoped { 42 }
        assertEquals(42, result)
    }
}
