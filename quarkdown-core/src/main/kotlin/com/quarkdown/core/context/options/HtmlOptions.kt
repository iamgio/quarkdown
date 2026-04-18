package com.quarkdown.core.context.options

/**
 * Options for HTML generation.
 * @param baseUrl the base URL to use for resolving relative paths in the generated HTML, without a trailing slash, e.g. `https://example.com`
 */
data class HtmlOptions(
    val baseUrl: String? = null,
)
