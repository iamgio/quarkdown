package eu.iamgio.quarkdown.pipeline.output

import eu.iamgio.quarkdown.pipeline.error.IOPipelineException

/**
 * Represents an [OutputArtifact] whose content is lazily loaded on demand once visited (via [accept]).
 * @param name name of the resource (without file extensions)
 * @param content supplier of the content of the resource, retrieved upon visit
 * @param type type of content the resource contains
 */
data class LazyOutputArtifact(
    override val name: String,
    override val content: () -> CharSequence,
    override val type: ArtifactType,
) : OutputArtifact<() -> CharSequence> {
    override fun <T> accept(visitor: OutputResourceVisitor<T>): T = visitor.visit(TextOutputArtifact(name, content(), type))

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
