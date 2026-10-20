package com.quarkdown.interaction.cdp

import com.quarkdown.core.log.Log
import com.quarkdown.interaction.Env
import com.quarkdown.interaction.executable.ChromiumWrapper
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import java.io.File
import java.util.Base64
import java.util.concurrent.TimeUnit
import kotlin.io.path.createTempDirectory

/**
 * Size of each chunk read from a browser-side output stream.
 */
private const val STREAM_CHUNK_SIZE = 1 shl 20

/**
 * Time to wait for the browser process to exit gracefully before killing it.
 */
private const val BROWSER_SHUTDOWN_TIMEOUT_SECONDS = 5L

/**
 * Default time to wait for the browser to announce its DevTools endpoint after startup.
 */
private const val DEFAULT_STARTUP_TIMEOUT_MS = 30_000L

/**
 * Delay between two consecutive polls of the browser's startup output.
 */
private const val STARTUP_POLL_INTERVAL_MS = 50L

/**
 * High-level interaction with a Chromium-family browser over the Chrome DevTools Protocol:
 * launches the browser process, connects to its default page target,
 * and shuts everything down when the interaction is over.
 * @param browser browser executable wrapper
 * @param noSandbox whether to disable the Chromium sandbox
 * @param startupTimeoutMs maximum time to wait for the browser to announce its DevTools endpoint
 */
class ChromiumInteraction(
    private val browser: ChromiumWrapper,
    private val noSandbox: Boolean = false,
    private val startupTimeoutMs: Long = DEFAULT_STARTUP_TIMEOUT_MS,
) {
    /**
     * Ensures the browser executable is available and working.
     * @throws IllegalStateException if the browser executable is not found or not valid
     */
    fun checkAvailability() {
        check(browser.isValid) {
            "Chrome executable cannot be found at '${browser.path}'.\n" +
                "Please install chrome-headless-shell (https://googlechromelabs.github.io/chrome-for-testing/) " +
                "or set --chrome-path (or the ${Env.QUARKDOWN_CHROME_PATH} environment variable) " +
                "to a Chromium-family browser executable."
        }
    }

    /**
     * Starts the browser with a fresh profile, connects to its default page target,
     * runs [action] against it, and finally kills the browser and cleans the profile up.
     * Blocking call.
     * @param action the interaction to perform on the page
     */
    fun withPage(action: suspend Page.() -> Unit) {
        val profileDirectory = createTempDirectory("quarkdown-browser-profile").toFile()
        var process: Process? = null
        try {
            process = browser.start(*browserArgs(profileDirectory))
            val browserWsUrl = awaitDevToolsAnnouncement(process)
            runBlocking {
                HttpClient(CIO) { install(WebSockets) }.use { client ->
                    val pageWsUrl = pageTargetWsUrl(client, browserWsUrl)
                    client.webSocket(pageWsUrl) {
                        Page(CdpConnection(this)).action()
                    }
                }
            }
        } finally {
            process?.let {
                it.destroy()
                if (!it.waitFor(BROWSER_SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                    it.destroyForcibly().waitFor(BROWSER_SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                }
            }
            if (!profileDirectory.deleteRecursively()) {
                Log.warn("Could not fully delete the temporary browser profile at $profileDirectory")
            }
        }
    }

    private fun browserArgs(profileDirectory: File): Array<String> =
        buildList {
            add("--headless")
            add("--disable-gpu")
            add("--hide-scrollbars")
            add("--remote-debugging-port=0")
            add("--user-data-dir=${profileDirectory.absolutePath}")
            if (noSandbox) add("--no-sandbox")
            add("about:blank")
        }.toTypedArray()

    /**
     * Reads the browser's output until it announces its DevTools WebSocket endpoint.
     * The output stream is intentionally left open: it stays attached to the running process.
     * @return the announced browser-level WebSocket URL
     * @throws IllegalStateException if the process exits, or [startupTimeoutMs] expires,
     *         before the announcement
     */
    private fun awaitDevToolsAnnouncement(process: Process): String {
        val reader = process.inputStream.bufferedReader()
        val currentLine = StringBuilder()
        val deadline = System.currentTimeMillis() + startupTimeoutMs
        while (true) {
            if (Thread.currentThread().isInterrupted) throw InterruptedException()
            when {
                reader.ready() -> {
                    when (val char = reader.read()) {
                        -1 -> {
                            continue
                        }

                        '\n'.code -> {
                            val line = currentLine.toString().removeSuffix("\r")
                            currentLine.clear()
                            Log.debug(line)
                            DevToolsEndpoint.wsUrlFromLogLine(line)?.let { return it }
                        }

                        else -> {
                            currentLine.append(char.toChar())
                        }
                    }
                }

                !process.isAlive -> {
                    throw IllegalStateException("Browser exited before announcing its DevTools endpoint")
                }

                System.currentTimeMillis() > deadline -> {
                    throw IllegalStateException(
                        "Browser did not announce its DevTools endpoint within ${startupTimeoutMs}ms",
                    )
                }

                else -> {
                    Thread.sleep(STARTUP_POLL_INTERVAL_MS)
                }
            }
        }
    }

    /**
     * @return the WebSocket debugger URL of the browser's default page target
     */
    private suspend fun pageTargetWsUrl(
        client: HttpClient,
        browserWsUrl: String,
    ): String {
        val body = client.get(DevToolsEndpoint.jsonListUrl(browserWsUrl)).bodyAsText()
        val page =
            Json
                .parseToJsonElement(body)
                .jsonArray
                .map { it.jsonObject }
                .firstOrNull { it["type"]?.jsonPrimitive?.content == "page" }
                ?: throw IllegalStateException("No page target exposed by the browser")
        return page["webSocketDebuggerUrl"]!!.jsonPrimitive.content
    }

    /**
     * Interaction with a single page target of a running browser.
     * @param cdp the protocol connection to the page's debugger
     */
    class Page internal constructor(
        private val cdp: CdpConnection,
    ) {
        /**
         * Navigates the page to [url].
         */
        suspend fun navigate(url: String) {
            cdp.call("Page.navigate", buildJsonObject { put("url", url) })
        }

        /**
         * Evaluates a JavaScript [expression] on the page.
         * @return the expression's value, or `null` if it is `undefined` or `null`
         */
        private suspend fun evaluate(expression: String): JsonElement? =
            cdp
                .call(
                    "Runtime.evaluate",
                    buildJsonObject {
                        put("expression", expression)
                        put("returnByValue", true)
                    },
                )["result"]!!
                .jsonObject["value"]
                ?.takeUnless { it is JsonNull }

        /**
         * Evaluates a JavaScript [expression] on the page.
         * @return the expression's boolean value
         */
        suspend fun evaluateBoolean(expression: String): Boolean = evaluate(expression)!!.jsonPrimitive.boolean

        /**
         * Evaluates a JavaScript [expression] on the page.
         * @return the expression's numeric value
         */
        suspend fun evaluateNumber(expression: String): Double = evaluate(expression)!!.jsonPrimitive.double

        /**
         * Evaluates a JavaScript [expression] on the page.
         * @return the expression's object value, or `null` if it is `undefined` or `null`
         */
        suspend fun evaluateObject(expression: String): JsonObject? = evaluate(expression)?.jsonObject

        /**
         * Emulates a CSS media type on the page, so that its media queries apply to on-screen rendering too.
         * @param media media type to emulate, e.g. `print`
         */
        suspend fun emulateMedia(media: String) {
            cdp.call("Emulation.setEmulatedMedia", buildJsonObject { put("media", media) })
        }

        /**
         * Overrides the size of the page's viewport, in CSS pixels, which the layout adapts to.
         * @param width viewport width
         * @param height viewport height
         */
        suspend fun setViewport(
            width: Int,
            height: Int,
        ) {
            cdp.call(
                "Emulation.setDeviceMetricsOverride",
                buildJsonObject {
                    put("width", width)
                    put("height", height)
                    put("deviceScaleFactor", 1)
                    put("mobile", false)
                },
            )
        }

        /**
         * Captures a screenshot of the whole page content, beyond the visible viewport.
         * @param params `Page.captureScreenshot` parameters, e.g. `format` or `quality`.
         *               The capture area is managed internally and must not be specified
         * @param out output image file to be written
         */
        suspend fun captureFullPageScreenshot(
            params: JsonObject,
            out: File,
        ) {
            val contentSize = cdp.call("Page.getLayoutMetrics")["cssContentSize"]!!.jsonObject
            val fullPageParams =
                buildJsonObject {
                    params.forEach { (key, value) -> put(key, value) }
                    put("captureBeyondViewport", true)
                    put(
                        "clip",
                        buildJsonObject {
                            put("x", 0)
                            put("y", 0)
                            put("width", contentSize["width"]!!)
                            put("height", contentSize["height"]!!)
                            put("scale", 1)
                        },
                    )
                }
            val data =
                cdp
                    .call("Page.captureScreenshot", fullPageParams)["data"]!!
                    .jsonPrimitive
                    .content
            out.writeBytes(Base64.getDecoder().decode(data))
        }

        /**
         * Prints the page to a PDF file, streamed in chunks to avoid holding the whole document in memory.
         * @param params `Page.printToPDF` parameters, e.g. `printBackground` or `paperHeight`.
         *               The transfer mode is managed internally and must not be specified
         * @param out output PDF file to be written
         */
        suspend fun printToPdf(
            params: JsonObject,
            out: File,
        ) {
            val streamParams =
                buildJsonObject {
                    params.forEach { (key, value) -> put(key, value) }
                    put("transferMode", "ReturnAsStream")
                }
            val stream =
                cdp
                    .call("Page.printToPDF", streamParams)["stream"]!!
                    .jsonPrimitive
                    .content
            readStreamToFile(stream, out)
        }

        /**
         * Reads the browser-side stream in chunks and writes it to [out].
         */
        private suspend fun readStreamToFile(
            streamHandle: String,
            out: File,
        ) {
            out.outputStream().use { output ->
                while (true) {
                    val chunk =
                        cdp.call(
                            "IO.read",
                            buildJsonObject {
                                put("handle", streamHandle)
                                put("size", STREAM_CHUNK_SIZE)
                            },
                        )
                    val data = chunk["data"]!!.jsonPrimitive.content
                    val bytes =
                        when {
                            chunk["base64Encoded"]?.jsonPrimitive?.boolean == true -> Base64.getDecoder().decode(data)
                            else -> data.toByteArray()
                        }
                    output.write(bytes)
                    if (chunk["eof"]?.jsonPrimitive?.boolean == true) break
                }
            }
            cdp.call("IO.close", buildJsonObject { put("handle", streamHandle) })
        }
    }
}
