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
    private val out: File,
    private val node: NodeJsWrapper,
    private val npm: NpmWrapper,
) {
    fun launch() {
        LocalFileWebServer(out.parentFile).start(SERVER_PORT) {
            Log.info("PDF server is ready.")
            checkPuppeteer()
            println("out: $out")
            val scriptFile = copyScript()
            println("scriptFile: $scriptFile")
            runScript(scriptFile)
            Log.info("PDF generated at ${out.absolutePath}")
        }
    }

    private fun checkPuppeteer() {
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

    private fun runScript(scriptFile: File) {
        val url = "http://localhost:$SERVER_PORT"
        node.evalFile(scriptFile, out.absolutePath, url)
    }
}
