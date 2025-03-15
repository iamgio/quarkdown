package eu.iamgio.quarkdown.pdf.html

import eu.iamgio.quarkdown.log.Log
import eu.iamgio.quarkdown.pdf.html.executable.NodeJsWrapper
import eu.iamgio.quarkdown.pdf.html.executable.NpmWrapper
import eu.iamgio.quarkdown.pdf.html.executable.PuppeteerNodeModule
import eu.iamgio.quarkdown.server.LocalFileWebServer
import java.io.File

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
        LocalFileWebServer(directory).start(SERVER_PORT) { server ->
            Log.info("PDF server is ready.")
            linkPuppeteer()
            scriptFile = copyScript()
            runScript()
            cleanup()
            Log.info("PDF generated at $out")
            server.stop()
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

    private fun cleanup() {
        scriptFile.delete()

        sequenceOf("package.json", "package-lock.json", "node_modules")
            .map { File(directory, it) }
            .forEach { it.deleteRecursively() }
    }
}
