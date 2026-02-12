package com.quarkdown.cli.creator.content

import com.quarkdown.core.pipeline.output.ArtifactType
import com.quarkdown.core.pipeline.output.LazyOutputArtifact
import com.quarkdown.core.pipeline.output.OutputResource

private const val RESOURCES_PATH = "/creator/docs/"
private val RESOURCES = arrayOf("_nav.qd", "main.qd", "page-1.qd", "page-2.qd", "page-3.qd")

/**
 * A [ProjectCreatorInitialContentSupplier] that provides a template for a documentation project via the `docs` library.
 */
class DocsProjectCreatorInitialContentSupplier : ProjectCreatorInitialContentSupplier {
    override val templateCodeContent: String
        get() = ""

    override fun createResources(): Set<OutputResource> =
        RESOURCES
            .map { page ->
                LazyOutputArtifact.internal(
                    RESOURCES_PATH + page,
                    page,
                    ArtifactType.AUTO,
                )
            }.toSet()
}
