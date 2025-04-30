package com.quarkdown.cli

import java.io.File

/**
 * Options that affect the behavior of the Quarkdown CLI, especially I/O.
 * For pipeline-related options, see [com.quarkdown.core.pipeline.PipelineOptions].
 * @param source main source file to process
 * @param outputDirectory the output directory to save resource in, if set
 * @param libraryDirectory the directory to load .qmd library files from
 * @param clean whether to clean the output directory before generating new files
 * @param nodePath path to the Node.js executable
 * @param npmPath path to the npm executable
 */
data class CliOptions(
    val source: File?,
    val outputDirectory: File?,
    val libraryDirectory: File?,
    val clean: Boolean,
    val nodePath: String?,
    val npmPath: String?,
)
