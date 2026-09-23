package com.quarkdown.core.pipeline.output

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
}
