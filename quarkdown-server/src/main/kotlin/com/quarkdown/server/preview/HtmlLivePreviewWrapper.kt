package com.quarkdown.server.preview

import com.quarkdown.template.TemplateProcessor

/**
 * Name of the precompiled JTE template that renders the live-preview HTML wrapper.
 */
private const val TEMPLATE_NAME = "live-preview/wrapper.html.jte"

private const val TEMPLATE_SOURCE_FILE_PLACEHOLDER = "srcFile"
private const val TEMPLATE_SERVER_HOST_PLACEHOLDER = "serverHost"
private const val TEMPLATE_SERVER_PORT_PLACEHOLDER = "serverPort"

/**
 * Renders the live-preview HTML wrapper, which embeds a target document inside double-buffered iframes and scroll preservation.
 *
 * - When [reloadSource] is provided, the wrapper additionally subscribes to a Server-Sent Events stream on the given
 *   host and port and refreshes the embedded page whenever a reload event arrives. This is the form served by the
 *   live preview endpoint (`/live/<file>`).
 * - When [reloadSource] is `null`, the wrapper carries no reload client and the embedded page must be refreshed
 *   by other means. This is the form written to disk by serverless preview strategies.
 *
 * @param srcFile URL or path used as the iframe's `src`. Both absolute server paths (e.g. `/index.html`) and
 *                document-relative paths (e.g. `index.html`) are supported; the value is forwarded as-is to the template.
 * @param reloadSource optional coordinates of the reload event stream the wrapper should subscribe to
 */
class HtmlLivePreviewWrapper(
    private val srcFile: String,
    private val reloadSource: ReloadSource? = null,
) {
    /**
     * Renders the wrapper into a self-contained HTML document.
     * @return the wrapper HTML as a string, ready to be served or written to disk
     */
    fun render(): String =
        TemplateProcessor(TEMPLATE_NAME)
            .value(TEMPLATE_SOURCE_FILE_PLACEHOLDER, srcFile)
            .value(TEMPLATE_SERVER_HOST_PLACEHOLDER, reloadSource?.host)
            .value(TEMPLATE_SERVER_PORT_PLACEHOLDER, reloadSource?.port?.toString())
            .process()
            .toString()

    /**
     * Coordinates of the reload event stream the wrapper should subscribe to.
     * @param host host name or address the reload endpoint is reachable on (e.g. `localhost`)
     * @param port port number the reload endpoint is exposed on
     */
    data class ReloadSource(
        val host: String,
        val port: Int,
    )
}
