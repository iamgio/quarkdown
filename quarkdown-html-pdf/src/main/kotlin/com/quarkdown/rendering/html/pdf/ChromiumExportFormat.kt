package com.quarkdown.rendering.html.pdf

import com.quarkdown.interaction.cdp.ChromiumInteraction
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import java.io.File
import kotlin.math.ceil

/**
 * CSS media type emulated while capturing images, so that they match the PDF output.
 */
private const val IMAGE_MEDIA = "print"

/**
 * A file format a rendered HTML document can be exported to through a Chromium-family browser.
 * Each format is a strategy that captures a ready page into an output file.
 * @see ChromiumExportScript
 */
sealed interface ChromiumExportFormat {
    /**
     * File extension of the exported artifact, without the leading dot.
     */
    val extension: String

    /**
     * Human-readable name of the format, used in logs and error messages.
     */
    val displayName: String
        get() = extension.uppercase()

    /**
     * Captures the given page, which is expected to be fully loaded and initialized, into [out].
     * @param page page to capture
     * @param out output file to be written
     */
    suspend fun export(
        page: ChromiumInteraction.Page,
        out: File,
    )

    /**
     * PDF document, produced by the browser's print engine.
     * Paged documents are split into pages, while plain documents fit in a single page.
     */
    data object Pdf : ChromiumExportFormat {
        override val extension = "pdf"

        override suspend fun export(
            page: ChromiumInteraction.Page,
            out: File,
        ) = page.printToPdf(page.pdfParams(), out)

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

    /**
     * Raster image of the whole document, captured as a single full-page screenshot.
     * Print media is emulated during the capture, so that the image matches the look of the PDF output.
     * @param extension file extension, which is also the `Page.captureScreenshot` format identifier
     */
    sealed class Image(
        override val extension: String,
    ) : ChromiumExportFormat {
        override suspend fun export(
            page: ChromiumInteraction.Page,
            out: File,
        ) {
            val pageSize = page.pageSize()
            page.emulateMedia(IMAGE_MEDIA)
            pageSize?.let { (width, height) -> page.setViewport(width, height) }
            page.captureFullPageScreenshot(buildJsonObject { put("format", extension) }, out)
        }

        /**
         * @return the size of a page of the document, in whole CSS pixels, as exposed by the runtime's `getPageSize()`,
         *         or `null` if the document is not paginated
         */
        private suspend fun ChromiumInteraction.Page.pageSize(): Pair<Int, Int>? =
            evaluateObject("typeof window.getPageSize === 'function' ? window.getPageSize() : undefined")
                ?.let { size -> size.px("width") to size.px("height") }

        private fun JsonObject.px(key: String): Int = ceil(getValue(key).jsonPrimitive.double).toInt()
    }

    /**
     * Lossless PNG image.
     */
    data object Png : Image("png")

    /**
     * WebP image.
     */
    data object Webp : Image("webp")
}
