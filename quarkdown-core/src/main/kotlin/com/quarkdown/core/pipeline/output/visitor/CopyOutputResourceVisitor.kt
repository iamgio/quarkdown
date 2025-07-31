package com.quarkdown.core.pipeline.output.visitor

import com.quarkdown.core.pipeline.output.BinaryOutputArtifact
import com.quarkdown.core.pipeline.output.OutputResource
import com.quarkdown.core.pipeline.output.OutputResourceGroup
import com.quarkdown.core.pipeline.output.OutputResourceVisitor
import com.quarkdown.core.pipeline.output.TextOutputArtifact

/**
 * [OutputResourceVisitor] that creates a copy of the visited resource with a new name.
 * @param name new name for the copied resource
 */
class CopyOutputResourceVisitor(
    private val name: String,
) : OutputResourceVisitor<OutputResource> {
    override fun visit(artifact: TextOutputArtifact): OutputResource = artifact.copy(name = name)

    override fun visit(artifact: BinaryOutputArtifact): OutputResource = artifact.copy(name = name)

    override fun visit(group: OutputResourceGroup): OutputResource = group.copy(name = name)
}

/**
 * Creates a copy of [this] resource with the specified [name].
 * @param name new name for the copied resource
 * @return a copy of [this] resource with the specified name
 */
fun OutputResource.copy(name: String = this.name): OutputResource = accept(CopyOutputResourceVisitor(name))
