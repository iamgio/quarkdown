package com.quarkdown.core.pipeline.output

/**
 * Abstraction of an output entity produced by the pipeline.
 * A resource is saved to file via a [FileResourceExporter].
 */
interface OutputResource {
    /**
     * Name of the resource (without file extensions).
     */
    val name: String

    /**
     * Accepts a [visitor] to perform operations on the resource.
     * @param visitor visitor to accept
     * @return result of the visit operation
     */
    fun <T> accept(visitor: OutputResourceVisitor<T>): T
}
