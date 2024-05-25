package eu.iamgio.quarkdown.pipeline.error

/**
 * Error thrown when trying to access the pipeline of a context that does not have an attached pipeline.
 */
class UnattachedPipelineException : IllegalStateException("Context does not have an attached pipeline")
