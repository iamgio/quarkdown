package eu.iamgio.quarkdown.cli.creator

import eu.iamgio.quarkdown.pipeline.output.ArtifactType
import eu.iamgio.quarkdown.pipeline.output.LazyOutputArtifact
import eu.iamgio.quarkdown.pipeline.output.OutputResource
import eu.iamgio.quarkdown.pipeline.output.OutputResourceGroup
import eu.iamgio.quarkdown.pipeline.output.TextOutputArtifact
import eu.iamgio.quarkdown.template.TemplateProcessor

/**
 *
 */
class ProjectCreator(
    private val templateProcessorFactory: ProjectCreatorTemplateProcessorFactory,
    private val includeInitialContent: Boolean = false,
) {
    fun createResources(): List<OutputResource> {
        val template: TemplateProcessor = templateProcessorFactory.create(includeInitialContent)

        val main =
            TextOutputArtifact(
                "main.qmd",
                template.process().trim(),
                ArtifactType.QUARKDOWN,
            )

        return buildList {
            add(main)
            if (includeInitialContent) {
                val logo =
                    LazyOutputArtifact.internal(
                        "/creator/logo.png",
                        "logo.png",
                        ArtifactType.AUTO,
                    )
                val imageGroup = OutputResourceGroup("image", setOf(logo))
                add(imageGroup)
            }
        }
    }
}
