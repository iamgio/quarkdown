package eu.iamgio.quarkdown.cli

import java.io.File

/**
 * Options that affect the behavior of the Quarkdown CLI, especially I/O.
 * For pipeline-related options, see [eu.iamgio.quarkdown.pipeline.PipelineOptions].
 * @param source main source file to process. If not set, the program runs in REPL mode
 * @param outputDirectory the output directory to save resource in, if set
 */
data class CliOptions(
    val source: File?,
    val outputDirectory: File?,
)
