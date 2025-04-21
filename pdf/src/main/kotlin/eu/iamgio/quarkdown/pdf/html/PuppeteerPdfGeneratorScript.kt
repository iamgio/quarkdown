package eu.iamgio.quarkdown.pdf.html

import eu.iamgio.quarkdown.interaction.executable.NodeJsWrapper
import eu.iamgio.quarkdown.interaction.executable.NodeNpmHelper
import eu.iamgio.quarkdown.interaction.executable.NpmWrapper
import eu.iamgio.quarkdown.log.Log
import eu.iamgio.quarkdown.server.LocalFileWebServer
import eu.iamgio.quarkdown.server.withScanner
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
                    Log.info("PDF generated at $out")
                } catch (e: Exception) {
                    throw e
                } finally {
                    server.stop()
                }
            }
    }

    private fun runScript() {
        requireNotNull(port) { "PDF server port is not set" }

        val script = javaClass.getResourceAsStream("/html/pdf.js")!!
        val url = "http://localhost:$port/?print-pdf"

        node.eval(script.reader(), out.absolutePath, url, noSandbox.toString())
    }
}
