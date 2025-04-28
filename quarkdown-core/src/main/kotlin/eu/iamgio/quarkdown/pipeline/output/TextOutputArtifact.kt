package eu.iamgio.quarkdown.pipeline.output

/**
 * Represents an [OutputResource] that contains text data.
 * @param name name of the resource (without file extensions)
 * @param content content of the resource
 * @param type type of content the resource contains
 */
data class TextOutputArtifact(
    override val name: String,
    override val content: CharSequence,
    override val type: ArtifactType,
) : OutputArtifact<CharSequence> {
    override fun <T> accept(visitor: OutputResourceVisitor<T>): T = visitor.visit(this)
}
