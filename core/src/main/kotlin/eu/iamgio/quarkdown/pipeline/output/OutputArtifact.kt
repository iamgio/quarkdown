package eu.iamgio.quarkdown.pipeline.output

/**
 * Represents an [OutputResource] that contains data.
 * When visited by a [FileResourceExporter], this resource is exported to a file
 * whose extension is determined by the resource's [type].
 * @param T type of data the resource contains
 * @see TextOutputArtifact
 * @see BinaryOutputArtifact
 * @see LazyOutputArtifact
 */
interface OutputArtifact<T> : OutputResource {
    /**
     * Content data of the resource.
     */
    val content: T

    /**
     * Type of content the resource contains.
     */
    val type: ArtifactType
}
