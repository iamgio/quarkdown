package eu.iamgio.quarkdown.pipeline.output

/**
 * Possible types of content an [OutputArtifact] contains.
 * @see TypedOutputResource
 */
enum class ArtifactType {
    HTML,
    CSS,
    JAVASCRIPT,

    /**
     * In case the artifact name includes a file extension, the type does not need to be specified.
     */
    AUTO,
}

/**
 * An [OutputResource] that contains information about its type.
 * @see ArtifactType
 */
interface TypedOutputResource : OutputResource {
    val type: ArtifactType
}
