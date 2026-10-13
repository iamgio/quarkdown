package com.quarkdown.pm.net

import io.ktor.http.Url

/**
 * Raised when a [RemoteFetcher] cannot retrieve a resource.
 * @param url resource that was requested
 * @param message description of the failure
 * @param cause underlying failure, if any
 */
open class RemoteFetchException(
    val url: Url,
    message: String,
    cause: Throwable? = null,
) : RuntimeException("Failed to fetch $url: $message", cause) {
    /**
     * Raised when the requested resource does not exist, as opposed to being unreachable.
     * @param url resource that was requested
     * @param cause underlying failure, if any
     */
    class NotFound(
        url: Url,
        cause: Throwable? = null,
    ) : RemoteFetchException(url, "resource not found", cause)
}
