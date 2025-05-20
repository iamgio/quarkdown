package com.quarkdown.rendering.html.pdf

import com.quarkdown.core.log.Log
import com.quarkdown.interaction.executable.NodeJsWrapper
import com.quarkdown.interaction.executable.NodeNpmHelper
import com.quarkdown.interaction.executable.NpmWrapper
import com.quarkdown.server.LocalFileWebServer
import com.quarkdown.server.withScanner
import java.io.File

/**
 * The starting port to attempt to start the server on.
 * It is incremented until a free port is found.
 */
private const val STARTING_SERVER_PORT = 8096

/**
 * Script-like generator of a PDF from HTML through Puppeteer via Node.js.
 * @param directory directory containing the `index.html` file
 * @param out output PDF file to be written
 * @param node Node.js executable wrapper
 * @param npm NPM executable wrapper
 * @param noSandbox whether to disable Chrome sandbox for PDF export
 */
class PuppeteerPdfGeneratorScript(
    private val directory: File,
    private val out: File,
    private val node: NodeJsWrapper,
    private val npm: NpmWrapper,
    private val noSandbox: Boolean = false,
) {
    private var port: Int? = null

    /**
     * Launches Puppeteer to convert the webpage from [directory] into a PDF saved at [out].
     */
    fun launch() =
        NodeNpmHelper(node, npm).launch(PuppeteerNodeModule) {
            launchServer()
        }

    private fun launchServer() {
        LocalFileWebServer(directory)
            .withScanner()
            .attemptStartUntilPortAvailable(STARTING_SERVER_PORT) { server, port ->
                this.port = port
                Log.info("PDF server is ready on port $port.")
                try {
                    runScript()
                    Log.info("PDF generated successfully.")
                } catch (e: Exception) {
                    Log.error("Failed to export PDF: ${e.message}")
                    Log.debug(e)
                } finally {
                    server.stop()
                }
            }
    }

    private fun runScript() {
        requireNotNull(port) { "PDF server port is not set" }

        val script = javaClass.getResourceAsStream("/pdf/pdf.js")!!
        val url = "http://localhost:$port/?print-pdf"

        node.eval(script.reader(), out.absolutePath, url, noSandbox.toString())
    }
}
