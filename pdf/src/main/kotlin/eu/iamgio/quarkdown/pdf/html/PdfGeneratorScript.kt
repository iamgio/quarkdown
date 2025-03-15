package eu.iamgio.quarkdown.pdf.html

import eu.iamgio.quarkdown.log.Log
import eu.iamgio.quarkdown.pdf.html.executable.NodeJsWrapper
import eu.iamgio.quarkdown.pdf.html.executable.NpmWrapper
import eu.iamgio.quarkdown.pdf.html.executable.PuppeteerNodeModule
import eu.iamgio.quarkdown.server.LocalFileWebServer
import java.io.File
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.deleteRecursively

private const val SERVER_PORT = 8090

/**
 *
 */
class PdfGeneratorScript(
    private val directory: File,
    private val out: File,
    private val node: NodeJsWrapper,
    private val npm: NpmWrapper,
) {
    private lateinit var scriptFile: File

    fun launch() {
        checkNode()
        checkNpm()
        linkPuppeteer()
        scriptFile = copyScript()

        LocalFileWebServer(directory).start(SERVER_PORT) { server ->
            Log.info("PDF server is ready.")
            runScript()
            Log.info("PDF generated at $out")
            server.stop()
            cleanup()
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
        val url = "http://localhost:$SERVER_PORT"
        node.evalFile(scriptFile, out.absolutePath, url)
    }

    @OptIn(ExperimentalPathApi::class)
    private fun cleanup() {
        scriptFile.delete()

        // Path#deleteRecursively does not follow symlinks, while File#deleteRecursively does.
        // The symlinks contained in the node_modules directory point to the global packages,
        // and should not be deleted.
        sequenceOf("package.json", "package-lock.json", "node_modules")
            .map { File(directory, it) }
            .forEach { it.toPath().deleteRecursively() }
    }
}
