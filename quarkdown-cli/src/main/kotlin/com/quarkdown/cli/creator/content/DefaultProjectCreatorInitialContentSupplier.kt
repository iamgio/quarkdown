package com.quarkdown.cli.creator.content

import com.quarkdown.core.pipeline.output.ArtifactType
import com.quarkdown.core.pipeline.output.LazyOutputArtifact
import com.quarkdown.core.pipeline.output.OutputResource
import com.quarkdown.core.pipeline.output.OutputResourceGroup

private const val RESOURCES_PATH = "/creator/"
private const val CODE_TEMPLATE_NAME = "creator/initialcontent.qd.jte"
private const val LOGO = "logo.png"
private const val IMAGES_GROUP_NAME = "image"

/**
 * A [ProjectCreatorInitialContentSupplier] that provides some initial content for introduction purposes:
 * - A simple Quarkdown code snippet rendered from the `creator/initialcontent.qd.jte` template
 * - An image of the Quarkdown logo
 */
class DefaultProjectCreatorInitialContentSupplier : ProjectCreatorInitialContentSupplier {
    override val templateName: String = CODE_TEMPLATE_NAME

    private val imageGroup: OutputResource
        get() =
            OutputResourceGroup(
                IMAGES_GROUP_NAME,
                setOf(
                    LazyOutputArtifact.internal(
                        RESOURCES_PATH + LOGO,
                        LOGO,
                        ArtifactType.AUTO,
                    ),
                ),
            )

    override fun createResources(): Set<OutputResource> = setOf(imageGroup)
}
