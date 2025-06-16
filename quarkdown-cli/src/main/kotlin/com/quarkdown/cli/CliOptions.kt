package com.quarkdown.cli

import com.quarkdown.cli.renderer.RendererRetriever
import java.io.File

/**
 * Options that affect the behavior of the Quarkdown CLI, especially I/O.
 * For pipeline-related options, see [com.quarkdown.core.pipeline.PipelineOptions].
 * @param source main source file to process
 * @param outputDirectory the output directory to save resource in, if set
 * @param libraryDirectory the directory to load .qd library files from
 * @param rendererName name of the renderer to use to generate the output for
 * @param clean whether to clean the output directory before generating new files
 * @param nodePath path to the Node.js executable
 * @param npmPath path to the npm executable
 * @param generatePdf whether to generate a PDF file
 * @param noPdfSandbox whether to disable the Chrome sandbox for PDF export
 */
data class CliOptions(
    val source: File?,
    val outputDirectory: File?,
    val libraryDirectory: File?,
    val rendererName: String,
    val clean: Boolean,
    val nodePath: String,
    val npmPath: String,
    val exportPdf: Boolean = false,
    val noPdfSandbox: Boolean = false,
) {
    /**
     * The rendering target to generate the output for.
     * For instance HTML or PDF.
     */
    val renderer by lazy { RendererRetriever(this).getRenderer() }
}
