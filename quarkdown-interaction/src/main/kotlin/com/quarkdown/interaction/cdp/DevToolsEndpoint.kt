package com.quarkdown.interaction.cdp

/**
 * Discovery of the DevTools endpoints exposed by a Chromium process
 * launched with `--remote-debugging-port`.
 */
internal object DevToolsEndpoint {
    private val LOG_LINE_REGEX = Regex("DevTools listening on (ws://\\S+)")

    /**
     * @param line a line of the browser's startup output
     * @return the browser-level WebSocket URL announced by [line], or `null` if this is not the announcement line
     */
    fun wsUrlFromLogLine(line: String): String? = LOG_LINE_REGEX.find(line)?.groupValues?.get(1)

    /**
     * @param browserWsUrl the browser-level WebSocket URL, as returned by [wsUrlFromLogLine]
     * @return the HTTP URL of the `/json/list` endpoint on the same host and port,
     *         which lists the open page targets
     */
    fun jsonListUrl(browserWsUrl: String): String {
        val authority = browserWsUrl.removePrefix("ws://").substringBefore('/')
        return "http://$authority/json/list"
    }
}
