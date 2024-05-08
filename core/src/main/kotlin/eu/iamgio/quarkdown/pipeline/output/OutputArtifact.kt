package eu.iamgio.quarkdown.pipeline.output

import eu.iamgio.quarkdown.pipeline.error.IOPipelineException

/**
 * Represents an [OutputResource] that contains text data.
 * When visited by a [FileResourceExporter], this resource is exported to a file
 * whose extension is determined by the resource's [type].
 * @param name name of the resource (without file extensions)
 * @param content content of the resource
 * @param type type of content the resource contains
 */
data class OutputArtifact(
    override val name: String,
    val content: CharSequence,
    val type: ArtifactType,
) : OutputResource {
    override fun <T> accept(visitor: OutputResourceVisitor<T>): T = visitor.visit(this)
}

/**
 * Represents an [OutputArtifact] whose content is lazily loaded on demand once visited (via [accept]).
 * @param name name of the resource (without file extensions)
 * @param content supplier of the content of the resource, retrieved upon visit
 * @param type type of content the resource contains
 */
data class LazyOutputArtifact(
    override val name: String,
    val content: () -> CharSequence,
    val type: ArtifactType,
) : OutputResource {
    override fun <T> accept(visitor: OutputResourceVisitor<T>): T = visitor.visit(OutputArtifact(name, content(), type))

    companion object {
        /**
         * Creates a [LazyOutputArtifact] whose content is extracted from an internal resource.
         * @param resource path to the internal resource
         * @param name name of the resource (without file extensions)
         * @param type type of content the resource contains
         */
        fun internal(
            resource: String,
            name: String,
            type: ArtifactType,
        ) = LazyOutputArtifact(
            name,
            content = {
                LazyOutputArtifact::class.java.getResource(resource)
                    ?.readText()
                    ?: throw IOPipelineException("Resource $resource not found")
            },
            type,
        )
    }
}

/**
 * Possible types of content an [OutputArtifact] contains.
 */
enum class ArtifactType {
    HTML,
    CSS,
}
