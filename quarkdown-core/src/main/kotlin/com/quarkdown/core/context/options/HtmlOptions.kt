package com.quarkdown.core.context.options

import com.quarkdown.amber.annotations.Mergeable

/**
 * Options for HTML generation.
 * @param baseUrl the base URL to use for resolving relative paths in the generated HTML, without a trailing slash, e.g. `https://example.com`
 * @param title overrides the document name used in the HTML `<title>` tag. If `null`, the document name is used
 * @param llmsTxtContent the content to include in the `llms.txt` file, if any. If `null`, no `llms.txt` file is generated
 * @param isMarkdownMirrorAvailable whether `llms.txt` should link to Markdown files rather than HTML. Markdown generation is handled externally
 */
@Mergeable
data class HtmlOptions(
    val baseUrl: String? = null,
    val title: String? = null,
    val llmsTxtContent: String? = null,
    val isMarkdownMirrorAvailable: Boolean = false,
)
