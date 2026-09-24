package com.quarkdown.core.util

import io.ktor.http.URLParserException
import io.ktor.http.Url

private const val ANCHOR_DELIMITER = '#'

/**
 * Protocols that [toUrlOrNull] accepts, mirroring the JVM's built-in `java.net.URL` protocol handlers.
 *
 * Explicitly whitelisting is required because Ktor's [Url] parses any string leniently,
 * while URL detection must reject file paths (e.g. Windows' `C:\images`, whose drive letter
 * would otherwise be read as a scheme) and unsupported schemes such as `data:` URIs.
 */
private val SUPPORTED_PROTOCOLS = setOf("http", "https", "ftp", "file", "mailto", "jar")

/**
 * Strips the anchor (fragment) from a URL string.
 * @return a pair of the base URL and the anchor, or `null` if no anchor is present
 */
fun String.stripAnchor(): Pair<String, String>? =
    when (val anchorIndex = indexOf(ANCHOR_DELIMITER)) {
        -1 -> null
        else -> Pair(substring(0, anchorIndex), substring(anchorIndex + 1))
    }

/**
 * @return a URL from [this] string if it's a valid URL with a supported protocol, or `null` otherwise
 */
fun String.toUrlOrNull(): Url? {
    val protocol = substringBefore(':', missingDelimiterValue = "").lowercase()
    if (protocol !in SUPPORTED_PROTOCOLS) return null
    return try {
        Url(this)
    } catch (_: URLParserException) {
        null
    }
}

/**
 * Whether [this] string is a valid URL.
 */
val String.isUrl: Boolean
    get() = toUrlOrNull() != null
