package eu.iamgio.quarkdown.pipeline.error

/**
 * An exception thrown during any stage of the pipeline.
 * @param code error code. If the program is running in strict mode and thus is killed,
 *             it defines the process exit code
 */
open class PipelineException(message: String, val code: Int) : Exception(message)
