package eu.iamgio.quarkdown.cli.creator.content

import eu.iamgio.quarkdown.pipeline.output.ArtifactType
import eu.iamgio.quarkdown.pipeline.output.LazyOutputArtifact
import eu.iamgio.quarkdown.pipeline.output.OutputResource
import eu.iamgio.quarkdown.pipeline.output.OutputResourceGroup

private const val RESOURCES_PATH = "/creator/"
private const val CODE_CONTENT_PATH = RESOURCES_PATH + "initialcontent.qmd.template"
private const val LOGO = "logo.png"
private const val IMAGES_GROUP_NAME = "image"

class DefaultProjectCreatorInitialContentSupplier : ProjectCreatorInitialContentSupplier {
    override val templateCodeContent: String
        get() = javaClass.getResourceAsStream(CODE_CONTENT_PATH)!!.bufferedReader().readText()

    override fun createResources(): Set<OutputResource> =
        buildSet {
            val logo =
                LazyOutputArtifact.internal(
                    RESOURCES_PATH + LOGO,
                    LOGO,
                    ArtifactType.AUTO,
                )
            val imageGroup = OutputResourceGroup(IMAGES_GROUP_NAME, setOf(logo))
            add(imageGroup)
        }
}
