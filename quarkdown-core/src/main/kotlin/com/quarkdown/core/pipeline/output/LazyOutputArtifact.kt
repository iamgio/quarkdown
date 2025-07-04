package com.quarkdown.core.pipeline.output

import com.quarkdown.core.pipeline.error.IOPipelineException
import com.quarkdown.core.pipeline.output.LazyOutputArtifact.Companion.internal
import kotlin.reflect.KClass

/**
 * Represents a [BinaryOutputArtifact] whose content is lazily loaded on demand (via [accept]).
 * @param name name of the resource (without file extensions)
 * @param content supplier of the content of the resource, retrieved upon visit
 * @param type type of content the resource contains
 */
data class LazyOutputArtifact(
    override val name: String,
    override val content: () -> List<Byte>,
    override val type: ArtifactType,
) : OutputArtifact<() -> List<Byte>> {
    // When visited, the content is loaded and a [BinaryOutputArtifact] is created and visited instead.
    override fun <T> accept(visitor: OutputResourceVisitor<T>): T = visitor.visit(BinaryOutputArtifact(name, content(), type))

    companion object {
        private fun readInternalBytes(
            resource: String,
            referenceClass: KClass<*> = LazyOutputArtifact::class,
        ): List<Byte>? =
            referenceClass.java
                .getResource(resource)
                ?.readBytes()
                ?.toList()

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
                readInternalBytes(resource, referenceClass)
                    ?: throw IOPipelineException("Resource $resource not found")
            },
            type,
        )

        /**
         * Like [internal], but reads the resource instantly and returns `null` if it does not exist.
         * @see internal
         */
        fun internalOrNull(
            resource: String,
            name: String,
            type: ArtifactType,
            referenceClass: KClass<*> = LazyOutputArtifact::class,
        ): LazyOutputArtifact? =
            readInternalBytes(resource, referenceClass)?.let {
                LazyOutputArtifact(name, content = { it }, type)
            }
    }
}
