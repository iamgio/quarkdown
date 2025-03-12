package eu.iamgio.quarkdown.pdf

import eu.iamgio.quarkdown.context.MutableContext
import eu.iamgio.quarkdown.flavor.quarkdown.QuarkdownFlavor
import eu.iamgio.quarkdown.pdf.html.HtmlToPdfExporter
import eu.iamgio.quarkdown.pdf.html.executable.NodeJsWrapper
import eu.iamgio.quarkdown.pdf.html.executable.NpmWrapper
import eu.iamgio.quarkdown.pdf.html.executable.PuppeteerNodeModule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Tests for the PDF module.
 */
class PdfTest {
    @Test
    fun `corresponding exporter`() {
        val html = QuarkdownFlavor.rendererFactory.html(MutableContext(QuarkdownFlavor))
        val htmlToPdf = PdfExporters.getForRenderingTarget(html.postRenderer, PdfExportOptions())

        assertIs<HtmlToPdfExporter>(htmlToPdf)
    }

    @Test
    fun `nodejs wrapper`() {
        val wrapper = NodeJsWrapper()
        assertTrue(wrapper.isValid)
        assertEquals("Hello, Quarkdown!\n", wrapper.eval("console.log('Hello, Quarkdown!')"))
        assertEquals(
            "Hello, Quarkdown!\n".repeat(2),
            wrapper.eval(
                """
                function hello() {
                    console.log('Hello, Quarkdown!');
                }
                hello();
                hello();
                """.trimIndent(),
            ),
        )
    }

    @Test
    fun `nonexisting nodejs`() {
        val wrapper = NodeJsWrapper("quarkdown-nodejs-nonexisting-path")
        assertEquals(false, wrapper.isValid)
    }

    @Test
    fun `npm wrapper`() {
        val wrapper = NpmWrapper()
        assertTrue(wrapper.isValid)
    }

    @Test
    fun `nonexisting npm`() {
        val wrapper = NpmWrapper("quarkdown-npm-nonexisting-path")
        assertEquals(false, wrapper.isValid)
    }

    @Test
    fun `puppeteer not installed`() {
        val wrapper = NodeJsWrapper()
        assertEquals(false, PuppeteerNodeModule.isInstalled(wrapper))
    }
}
