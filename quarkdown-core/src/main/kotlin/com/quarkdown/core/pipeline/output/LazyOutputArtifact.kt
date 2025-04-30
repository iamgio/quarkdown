package com.quarkdown.core.pipeline.output

import com.quarkdown.core.pipeline.error.IOPipelineException
import kotlin.reflect.KClass

/**
 * Represents a [BinaryOutputArtifact] whose content is lazily loaded on demand (via [accept]).
 * @param name name of the resource (without file extensions)
 * @param content supplier of the content of the resource, retrieved upon visit
 * @param type type of content the resource contains
 */
data class LazyOutputArtifact(
    override val name: String,
    override val content: () -> ByteArray,
    override val type: ArtifactType,
) : OutputArtifact<() -> ByteArray> {
    // When visited, the content is loaded and a [BinaryOutputArtifact] is created and visited instead.
    override fun <T> accept(visitor: OutputResourceVisitor<T>): T = visitor.visit(BinaryOutputArtifact(name, content(), type))

    companion object {
        /**
         * Creates a [LazyOutputArtifact] whose content is extracted from an internal resource.
         * @param resource path to the internal resource
         * @param name name of the resource (without file extensions)
         * @param type type of content the resource contains
         * @param referenceClass reference classpath to use to retrieve the internal resource
         */
        fun internal(
            resource: String,
            name: String,
            type: ArtifactType,
            referenceClass: KClass<*> = LazyOutputArtifact::class,
        ) = LazyOutputArtifact(
            name,
            content = {
                referenceClass.java.getResource(resource)
                    ?.readBytes()
                    ?: throw IOPipelineException("Resource $resource not found")
            },
            type,
        )
    }
}
