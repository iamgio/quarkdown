package eu.iamgio.quarkdown.pipeline.output

/**
 * Possible types of content an [OutputArtifact] contains.
 */
enum class ArtifactType {
    HTML,
    CSS,
    JAVASCRIPT,

    /**
     * Quarkdown source file (QMD)
     */
    QUARKDOWN,

    /**
     * In case the artifact name includes a file extension, the type does not need to be specified.
     */
    AUTO,
}
