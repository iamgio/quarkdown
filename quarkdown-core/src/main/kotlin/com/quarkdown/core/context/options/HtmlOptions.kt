package com.quarkdown.core.context.options

import com.quarkdown.amber.annotations.Mergeable

/**
 * Options for HTML generation.
 * @param baseUrl the base URL to use for resolving relative paths in the generated HTML, without a trailing slash, e.g. `https://example.com`
 * @param title overrides the document name used in the HTML `<title>` tag. If `null`, the document name is used.
 */
@Mergeable
data class HtmlOptions(
    val baseUrl: String? = null,
    val title: String? = null,
)
