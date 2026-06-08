package com.quarkdown.server.preview

import com.quarkdown.template.TemplateProcessor

/**
 * Name of the precompiled JTE template that renders the live-preview HTML wrapper.
 */
private const val TEMPLATE_NAME = "live-preview/wrapper.html.jte"

private const val TEMPLATE_SOURCE_FILE_PLACEHOLDER = "srcFile"
private const val TEMPLATE_ENDPOINT_ROOT_PLACEHOLDER = "endpointRoot"

/**
 * Default endpoint root: the wrapper assumes the reload endpoint sits at `/<endpoint>` relative to
 * the document origin. Matches the layout used by the bundled Quarkdown server.
 */
const val DEFAULT_ENDPOINT_ROOT: String = "/"

/**
 * Renders the live-preview HTML wrapper, which embeds a target document inside double-buffered iframes
 * with scroll preservation, and subscribes to a Server-Sent Events reload stream.
 *
 * The reload URL is built by concatenating [endpointRoot] with the endpoint name (`reload`).
 *
 * @param srcFile URL or path used as the iframe's `src`
 * @param endpointRoot path prefix concatenated with the endpoint name to form the SSE URL. Defaults to
 *                     [DEFAULT_ENDPOINT_ROOT] (`/`); pass e.g. `"../"` to produce `../reload`.
 */
class HtmlLivePreviewWrapper(
    private val srcFile: String,
    private val endpointRoot: String = DEFAULT_ENDPOINT_ROOT,
) {
    /**
     * Renders the wrapper into a self-contained HTML document.
     * @return the wrapper HTML as a string, ready to be served or written to disk
     */
    fun render(): String =
        TemplateProcessor(TEMPLATE_NAME)
            .value(TEMPLATE_SOURCE_FILE_PLACEHOLDER, srcFile)
            .value(TEMPLATE_ENDPOINT_ROOT_PLACEHOLDER, endpointRoot)
            .process()
            .toString()
}
