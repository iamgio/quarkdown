package com.quarkdown.rendering.html.pdf

import com.quarkdown.core.log.Log
import com.quarkdown.interaction.cdp.ChromiumInteraction
import com.quarkdown.interaction.executable.ChromiumWrapper
import com.quarkdown.server.LocalFileWebServer
import com.quarkdown.server.withScanner
import kotlinx.coroutines.delay
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.io.File

/**
 * The starting port to attempt to start the server on.
 * It is incremented until a free port is found.
 */
private const val STARTING_SERVER_PORT = 8096

/**
 * Delay between two consecutive readiness polls of the page.
 */
private const val READINESS_POLL_INTERVAL_MS = 150L

/**
 * Script-like generator of a PDF from HTML through a Chromium-family browser,
 * driven over the Chrome DevTools Protocol via [ChromiumInteraction].
 * @param sourcesDirectory directory containing the `index.html` file
 * @param out output PDF file to be written
 * @param browser browser executable wrapper
 * @param noSandbox whether to disable the Chromium sandbox for PDF export
 */
class ChromiumPdfGeneratorScript(
    private val sourcesDirectory: File,
    private val out: File,
    browser: ChromiumWrapper,
    noSandbox: Boolean = false,
) {
    private val interaction = ChromiumInteraction(browser, noSandbox)

    /**
     * Launches the browser to convert the webpage from [sourcesDirectory] into a PDF saved at [out].
     * Blocking call.
     * @throws IllegalStateException if the browser executable is not found or not valid
     */
    fun launch() {
        interaction.checkAvailability()
        launchServer()
    }

    private fun launchServer() {
        LocalFileWebServer(sourcesDirectory)
            .withScanner()
            .attemptStartUntilPortAvailable(STARTING_SERVER_PORT) { server, port ->
                Log.info("PDF server is ready on port $port. Please wait...")
                try {
                    generate("http://localhost:$port/?print-pdf")
                    Log.info("PDF generated successfully.")
                } catch (e: InterruptedException) {
                    throw e
                } catch (e: Exception) {
                    Log.error("Failed to export PDF: ${e.message}")
                    Log.debug(e)
                } finally {
                    server.stop()
                }
            }
    }

    private fun generate(url: String) =
        interaction.withPage {
            navigate(url)
            awaitReady()
            printToPdf(pdfParams(), out)
        }

    /**
     * Suspends until the page signals completed pagination (paged.js)
     * and runtime initialization via `window.isReady()`.
     */
    private suspend fun ChromiumInteraction.Page.awaitReady() {
        while (!evaluateBoolean("typeof window.isReady === 'function' && window.isReady() === true")) {
            delay(READINESS_POLL_INTERVAL_MS)
        }
    }

    /**
     * @return the `Page.printToPDF` parameters for the current page
     */
    private suspend fun ChromiumInteraction.Page.pdfParams(): JsonObject =
        buildJsonObject {
            put("printBackground", true)
            put("preferCSSPageSize", true)
            // Plain documents render as a single-page PDF.
            if (evaluateBoolean("document.body.classList.contains('quarkdown-plain')")) {
                val bodyHeight = evaluateNumber("document.body.clientHeight")
                put("paperHeight", PdfPaperSize.singlePageHeightInches(bodyHeight))
            }
        }
}
