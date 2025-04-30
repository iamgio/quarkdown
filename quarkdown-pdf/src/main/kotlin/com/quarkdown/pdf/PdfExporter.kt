package com.quarkdown.pdf

import java.io.File

/**
 * Strategy for exporting a PDF.
 * @see PdfExporters
 * @see com.quarkdown.pdf.html.HtmlPdfExporter
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
