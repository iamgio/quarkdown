package com.quarkdown.rendering.html.pdf

import com.quarkdown.interaction.executable.ChromiumWrapper
import org.junit.Assume.assumeTrue
import java.io.File
import javax.imageio.ImageIO
import kotlin.io.path.createTempDirectory
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for HTML-to-image (PNG, WebP) generation.
 */
class HtmlToImageTest {
    private val directory: File =
        createTempDirectory()
            .toFile()

    private val options =
        ChromiumExportOptions(
            outputDirectory = directory,
            chromePath = ChromiumWrapper.defaultPath,
            noSandbox = true,
        )

    @BeforeTest
    fun setup() {
        directory.deleteRecursively()
        directory.mkdirs()
        assumeTrue(runCatching { ChromiumWrapper(options.chromePath).isValid }.getOrDefault(false))
    }

    /**
     * Writes an `index.html` test page whose body is [body], styled by [style],
     * optionally exposing [pageSize] as the runtime's `getPageSize()`.
     */
    private fun writePage(
        style: String,
        body: String,
        pageSize: String? = null,
    ) {
        File(directory, "index.html").writeText(
            """
            <!DOCTYPE html>
            <html>
            <head>
                <title>Test</title>
                <style>body { margin: 0; } $style</style>
            </head>
            <body>
                $body
                <script>
                window.isReady = () => true;
                ${pageSize?.let { "window.getPageSize = () => ($it);" } ?: ""}
                </script>
            </body>
            </html>
            """.trimIndent(),
        )
    }

    private fun export(format: ChromiumExportFormat): File {
        val out = File(directory, "out.${format.extension}")
        HtmlChromiumExporter(options, format).export(directory, out)
        assertTrue(out.exists())
        return out
    }

    @Test
    fun `png captures the whole page under print media`() {
        writePage(
            style = "div { width: 300px; height: 2000px; background: red; } @media print { div { background: blue; } }",
            body = "<div></div>",
        )

        val image = ImageIO.read(export(ChromiumExportFormat.Png))

        assertEquals(2000, image.height)
        assertTrue(image.width >= 300)
        assertEquals(0x0000FF, image.getRGB(150, 1500) and 0xFFFFFF)
    }

    @Test
    fun `png sizes the viewport to a page of paginated documents`() {
        writePage(
            style = "div { width: 500px; height: 100vh; } .a { background: red; } .b { background: blue; }",
            body = "<div class='a'></div><div class='b'></div><div class='a'></div>",
            pageSize = "{ width: 500, height: 400 }",
        )

        val image = ImageIO.read(export(ChromiumExportFormat.Png))

        assertEquals(500, image.width)
        assertEquals(1200, image.height)
        assertEquals(0xFF0000, image.getRGB(250, 200) and 0xFFFFFF)
        assertEquals(0x0000FF, image.getRGB(250, 600) and 0xFFFFFF)
        assertEquals(0xFF0000, image.getRGB(250, 1000) and 0xFFFFFF)
    }

    @Test
    fun `webp is produced with its signature`() {
        writePage(style = "", body = "<p>Hello</p>")

        val bytes = export(ChromiumExportFormat.Webp).readBytes()

        assertContentEquals("RIFF".toByteArray(), bytes.copyOfRange(0, 4))
        assertContentEquals("WEBP".toByteArray(), bytes.copyOfRange(8, 12))
    }
}
