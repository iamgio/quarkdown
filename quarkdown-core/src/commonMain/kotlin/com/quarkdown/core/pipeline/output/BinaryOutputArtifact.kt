package com.quarkdown.core.pipeline.output

/**
 * Represents an [OutputResource] that contains binary data.
 * @param name name of the resource (without file extensions)
 * @param content binary content of the resource
 * @param type type of content the resource contains
 */
data class BinaryOutputArtifact(
    override val name: String,
    override val content: List<Byte>,
    override val type: ArtifactType,
) : OutputArtifact<List<Byte>> {
    override fun <T> accept(visitor: OutputResourceVisitor<T>): T = visitor.visit(this)
}
