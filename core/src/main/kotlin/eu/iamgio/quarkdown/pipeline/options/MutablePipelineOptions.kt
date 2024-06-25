package eu.iamgio.quarkdown.pipeline.options

import eu.iamgio.quarkdown.pipeline.error.BasePipelineErrorHandler
import eu.iamgio.quarkdown.pipeline.error.PipelineErrorHandler
import eu.iamgio.quarkdown.pipeline.error.StrictPipelineErrorHandler
import java.io.File

/**
 * Mutable implementation of [PipelineOptions].
 * @param exitOnError whether the process should be aborted when an error is caught
 * @param outputPath location of the output directory
 */
data class MutablePipelineOptions(
    override var prettyOutput: Boolean = false,
    override var wrapOutput: Boolean = true,
    var exitOnError: Boolean = false,
    var outputPath: String? = null,
) : PipelineOptions {
    override val errorHandler: PipelineErrorHandler
        get() = if (exitOnError) StrictPipelineErrorHandler() else BasePipelineErrorHandler()

    override val outputDirectory: File?
        get() = outputPath?.let(::File)
}
