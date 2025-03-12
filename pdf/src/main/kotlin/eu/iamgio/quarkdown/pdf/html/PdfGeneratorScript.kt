package eu.iamgio.quarkdown.pdf.html

import eu.iamgio.quarkdown.log.Log
import eu.iamgio.quarkdown.pdf.html.executable.NodeJsWrapper
import eu.iamgio.quarkdown.pdf.html.executable.NpmWrapper
import eu.iamgio.quarkdown.pdf.html.executable.PuppeteerNodeModule
import java.io.File

/**
 *
 */
object PdfGeneratorScript {
    fun launch(
        out: File,
        url: String,
        node: NodeJsWrapper,
        npm: NpmWrapper,
    ) {
        if (!npm.isInstalled(PuppeteerNodeModule)) {
            Log.info("Puppeteer is not installed. Installing...")
            npm.install(PuppeteerNodeModule)
        }

        npm.link(node, PuppeteerNodeModule)

        // Copy pdf.js into the working directory to run it via Node.js.
        val scriptFile = File(out.parentFile, "pdf.js")
        javaClass.getResourceAsStream("/html/pdf.js")!!.copyTo(scriptFile.outputStream())

        node.evalFile(scriptFile, out.absolutePath, url)
    }
}
