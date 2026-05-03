package com.quarkdown.cli.exec

/**
 * Thrown when the execution exceeds the allowed [timeoutSeconds].
 */
class ExecutionTimeoutException(
    val timeoutSeconds: Int,
) : RuntimeException("Execution timed out after $timeoutSeconds seconds.")
