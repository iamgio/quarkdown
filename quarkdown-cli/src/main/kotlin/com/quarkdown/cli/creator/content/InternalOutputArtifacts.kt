package com.quarkdown.cli.creator.content

import com.quarkdown.core.pipeline.error.IOPipelineException
import com.quarkdown.core.pipeline.output.ArtifactType
import com.quarkdown.core.pipeline.output.LazyOutputArtifact

/**
 * Creates a [LazyOutputArtifact] whose content is extracted on demand from an internal classpath resource.
 * @param resource path to the internal resource
 * @param name name of the resource (without file extensions)
 * @param type type of content the resource contains
 * @throws IOPipelineException upon content retrieval if the resource does not exist
 */
fun internalOutputArtifact(
    resource: String,
    name: String,
    type: ArtifactType,
): LazyOutputArtifact =
    LazyOutputArtifact(
        name,
        content = {
            val url =
                object {}.javaClass.getResource(resource)
                    ?: throw IOPipelineException("Resource $resource not found")
            url.readBytes().toList()
        },
        type,
    )
