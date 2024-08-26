package eu.iamgio.quarkdown.pipeline.output

/**
 * Represents an [OutputResource] that contains binary data.
 * When visited by a [FileResourceExporter], this resource is exported to a file
 * whose extension is determined by the resource's [type].
 * @param name name of the resource (without file extensions)
 * @param content binary content of the resource
 * @param type type of content the resource contains
 */
class BinaryOutputArtifact(
    override val name: String,
    val content: ByteArray,
    override val type: ArtifactType,
) : TypedOutputResource {
    override fun <T> accept(visitor: OutputResourceVisitor<T>): T = visitor.visit(this)
}
