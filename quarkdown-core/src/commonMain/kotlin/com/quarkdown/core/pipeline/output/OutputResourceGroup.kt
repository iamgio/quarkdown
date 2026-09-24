package com.quarkdown.core.pipeline.output

/**
 * Represents an [OutputResource] that contains other resources (and does not provide content of its own).
 * When visited by a [FileResourceExporter], this resource is exported to a directory.
 * @param name name of the resource (without file extensions)
 * @param resources sub-resources this group contains
 */
data class OutputResourceGroup(
    override val name: String,
    val resources: Set<OutputResource>,
) : OutputResource {
    override fun <T> accept(visitor: OutputResourceVisitor<T>): T = visitor.visit(this)
}
