package eu.iamgio.quarkdown.pdf

import eu.iamgio.quarkdown.pdf.html.HtmlPdfExporter
import java.io.File

/**
 * Strategy for exporting a PDF.
 * @see PdfExporters
 * @see HtmlPdfExporter
 */
interface PdfExporter {
    /**
     * Converts a directory, which contains the output of Quarkdown's compilation, into a PDF file.
     * @param directory directory containing Quarkdown's output
     * @param out output PDF file to be written
     */
    fun export(
        directory: File,
        out: File,
    )
}
