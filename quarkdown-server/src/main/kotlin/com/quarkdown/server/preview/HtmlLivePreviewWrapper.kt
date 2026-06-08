package com.quarkdown.server.preview

import com.quarkdown.template.TemplateProcessor

/**
 * Name of the precompiled JTE template that renders the live-preview HTML wrapper.
 */
private const val TEMPLATE_NAME = "live-preview/wrapper.html.jte"

private const val TEMPLATE_SOURCE_FILE_PLACEHOLDER = "srcFile"

/**
 * Renders the live-preview HTML wrapper, which embeds a target document inside double-buffered iframes
 * with scroll preservation, and subscribes to a same-origin Server-Sent Events reload stream at
 * `/reload`. The wrapper is meant to be served by a Quarkdown server: the SSE subscription always
 * resolves against the document's own origin, so it inherits whatever host:port the wrapper was loaded
 * from (`localhost`, `127.0.0.1`, an embedder-picked loopback, etc.) without any CORS coordination.
 *
 * @param srcFile URL or path used as the iframe's `src`
 */
class HtmlLivePreviewWrapper(
    private val srcFile: String,
) {
    /**
     * Renders the wrapper into a self-contained HTML document.
     * @return the wrapper HTML as a string, ready to be served or written to disk
     */
    fun render(): String =
        TemplateProcessor(TEMPLATE_NAME)
            .value(TEMPLATE_SOURCE_FILE_PLACEHOLDER, srcFile)
            .process()
            .toString()
}
