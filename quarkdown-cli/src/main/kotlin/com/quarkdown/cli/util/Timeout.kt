package com.quarkdown.cli.util

import com.quarkdown.cli.exec.ExecutionTimeoutException
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

/**
 * Runs [action] with an optional timeout.
 * If [timeoutSeconds] is `null`, [action] is executed directly on the current thread.
 * Otherwise, it is submitted and bounded by the given duration.
 * @param timeoutSeconds maximum allowed seconds, or `null` to disable
 * @param action the computation to run
 * @return the result of [action]
 * @throws ExecutionTimeoutException if the timeout is exceeded
 */
fun <T> runWithTimeout(
    timeoutSeconds: Int?,
    action: () -> T,
): T {
    if (timeoutSeconds == null || timeoutSeconds <= 0) {
        return action()
    }

    val executor =
        Executors.newSingleThreadExecutor { runnable ->
            Thread(runnable).apply { isDaemon = true }
        }
    val future = executor.submit(action)

    return try {
        future.get(timeoutSeconds.toLong(), TimeUnit.SECONDS)
    } catch (_: TimeoutException) {
        future.cancel(true)
        throw ExecutionTimeoutException(timeoutSeconds)
    } catch (e: ExecutionException) {
        throw e.cause ?: e
    } finally {
        executor.shutdownNow()
    }
}
