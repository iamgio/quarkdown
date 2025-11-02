package com.quarkdown.cli.util

/**
 * Simple immutable stopwatch to measure elapsed time in milliseconds.
 */
class MillisStopwatch {
    private val startTime: Long = System.currentTimeMillis()

    /**
     * Returns the elapsed time in milliseconds since the creation of this stopwatch.
     * @return elapsed time in milliseconds
     */
    fun elapsedMillis(): Long = System.currentTimeMillis() - startTime
}
