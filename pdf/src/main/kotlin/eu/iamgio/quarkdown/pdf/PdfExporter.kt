package eu.iamgio.quarkdown.pdf

import java.io.File

/**
 *
 */
interface PdfExporter {
    fun export(out: File)
}
