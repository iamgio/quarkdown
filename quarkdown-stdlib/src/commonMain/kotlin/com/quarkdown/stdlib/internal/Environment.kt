package com.quarkdown.stdlib.internal

/**
 * @param name name of the environment variable
 * @return the value of the variable, or `null` if it is not set
 * @throws com.quarkdown.core.platform.UnsupportedPlatformOperationException on platforms without an environment
 */
internal expect fun environmentVariable(name: String): String?
