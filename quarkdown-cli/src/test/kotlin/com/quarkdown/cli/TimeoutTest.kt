package com.quarkdown.cli

import com.quarkdown.cli.exec.ExecutionTimeoutException
import com.quarkdown.cli.util.runWithTimeout
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Unit tests for [runWithTimeout].
 */
class TimeoutTest {
    @Test
    fun `returns result when action completes within timeout`() {
        val result = runWithTimeout(5) { 42 }
        assertEquals(42, result)
    }

    @Test
    fun `runs action directly when timeout is null`() {
        val result = runWithTimeout(null) { "hello" }
        assertEquals("hello", result)
    }

    @Test
    fun `throws ExecutionTimeoutException when action exceeds timeout`() {
        assertFailsWith<ExecutionTimeoutException> {
            runWithTimeout(1) {
                Thread.sleep(5000)
            }
        }
    }

    @Test
    fun `propagates exception thrown by action`() {
        assertFailsWith<IllegalStateException> {
            runWithTimeout(5) {
                throw IllegalStateException("something went wrong")
            }
        }
    }
}
