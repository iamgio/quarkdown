package com.quarkdown.core.pipeline.output

/**
 * Visitor for [OutputResource] types.
 * @param T return type of visit operations
 */
interface OutputResourceVisitor<T> {
    fun visit(artifact: TextOutputArtifact): T

    fun visit(artifact: BinaryOutputArtifact): T

    fun visit(group: OutputResourceGroup): T
}
