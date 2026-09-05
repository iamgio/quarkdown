package com.quarkdown.cli

import com.quarkdown.cli.renderer.RendererRetriever
import java.io.File

/**
 * Options that affect the behavior of the Quarkdown CLI, especially I/O.
 * For pipeline-related options, see [com.quarkdown.core.pipeline.PipelineOptions].
 * @param source main source file to process
 * @param outputDirectory the output directory to save resource in, if set
 * @param libraryDirectory the directory to load `.qd` library files from. Defaults to `lib/qd` within the install layout
 * @param rendererName name of the renderer to use to generate the output for
 * @param clean whether to clean the output directory before generating new files
 * @param pipe whether to output the rendered result to standard output, suitable for piping
 * @param chromePath path to the Chromium-family browser executable, used for PDF export
 * @param exportPdf whether to generate a PDF file
 * @param noPdfSandbox whether to disable the Chrome sandbox for PDF export
 * @param timeoutSeconds maximum time, in seconds, allowed for the pipeline execution to complete.
 * `null` (default) or non-positive disables the timeout and runs the pipeline inline on the current thread.
 */
data class CliOptions(
    val source: File?,
    val outputDirectory: File?,
    val libraryDirectory: File?,
    val rendererName: String,
    val clean: Boolean,
    val pipe: Boolean,
    val chromePath: String,
    val exportPdf: Boolean = false,
    val noPdfSandbox: Boolean = false,
    val timeoutSeconds: Int? = null,
) {
    /**
     * The rendering target to generate the output for.
     * For instance HTML or PDF.
     */
    val renderer by lazy { RendererRetriever(this).getRenderer() }
}
