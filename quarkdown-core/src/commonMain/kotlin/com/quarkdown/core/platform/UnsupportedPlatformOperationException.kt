package com.quarkdown.core.platform

/**
 * Raised when an operation is requested on a platform that cannot provide it,
 * such as reading environment variables or the disk from a browser.
 * @param operation human-readable description of the operation, e.g. `Reading environment variables`
 */
class UnsupportedPlatformOperationException(
    operation: String,
) : UnsupportedOperationException("$operation is not supported on this platform.")
