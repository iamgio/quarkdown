package eu.iamgio.quarkdown.pipeline.output

/**
 * Visitor for [OutputResource] types.
 * @param T return type of visit operations
 */
interface OutputResourceVisitor<T> {
    fun visit(artifact: OutputArtifact): T

    fun visit(group: OutputResourceGroup): T
}
