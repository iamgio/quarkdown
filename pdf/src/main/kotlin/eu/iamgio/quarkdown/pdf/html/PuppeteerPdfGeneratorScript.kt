package eu.iamgio.quarkdown.pdf.html

import eu.iamgio.quarkdown.log.Log
import eu.iamgio.quarkdown.pdf.html.executable.NodeJsWrapper
import eu.iamgio.quarkdown.pdf.html.executable.NpmWrapper
import eu.iamgio.quarkdown.pdf.html.executable.PuppeteerNodeModule
import eu.iamgio.quarkdown.server.LocalFileWebServer
import eu.iamgio.quarkdown.server.withScanner
import java.io.File
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.deleteRecursively

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
 */
class PuppeteerPdfGeneratorScript(
    private val directory: File,
    private val out: File,
    private val node: NodeJsWrapper,
    private val npm: NpmWrapper,
) {
    private lateinit var scriptFile: File
    private var port: Int? = null

    /**
     * Launches Puppeteer to convert the webpage from [directory] into a PDF saved at [out].
     */
    fun launch() {
        checkNode()
        checkNpm()
        linkPuppeteer()
        scriptFile = copyScript()

        LocalFileWebServer(directory).withScanner().attemptStartUntilPortAvailable(STARTING_SERVER_PORT) { server, port ->
            this.port = port
            Log.info("PDF server is ready on port $port.")
            try {
                runScript()
                Log.info("PDF generated at $out")
            } catch (e: Exception) {
                throw e
            } finally {
                server.stop()
                cleanup()
            }
        }
    }

    private fun checkNode() {
        if (!node.isValid) {
            throw IllegalStateException("Node.js cannot be found at '${node.path}'")
        }
    }

    private fun checkNpm() {
        if (!npm.isValid) {
            throw IllegalStateException("NPM cannot be found at '${npm.path}'")
        }
    }

    private fun linkPuppeteer() {
        if (!npm.isInstalled(PuppeteerNodeModule)) {
            Log.info("Puppeteer is not installed. Installing...")
            npm.install(PuppeteerNodeModule)
        }
        npm.link(node, PuppeteerNodeModule)
    }

    /**
     * Copies pdf.js into the working directory.
     * @return the copied file
     */
    private fun copyScript() =
        File(out.parentFile, "pdf.js").also {
            javaClass.getResourceAsStream("/html/pdf.js")!!.copyTo(it.outputStream())
        }

    private fun runScript() {
        requireNotNull(port) { "PDF server port is not set" }

        val url = "http://localhost:$port/?print-pdf"
        node.evalFile(scriptFile, out.absolutePath, url)
    }

    @OptIn(ExperimentalPathApi::class)
    private fun cleanup() {
        scriptFile.delete()

        // Path#deleteRecursively does not follow symlinks, while File#deleteRecursively does.
        // The symlinks contained in the node_modules directory point to the global packages,
        // and should not be deleted.
        sequenceOf("package.json", "package-lock.json", "node_modules")
            .map { File(directory, it).toPath() }
            .forEach { it.deleteRecursively() }
    }
}
