package eu.iamgio.quarkdown.cli.creator

import eu.iamgio.quarkdown.cli.creator.content.ProjectCreatorInitialContentSupplier
import eu.iamgio.quarkdown.pipeline.output.ArtifactType
import eu.iamgio.quarkdown.pipeline.output.OutputResource
import eu.iamgio.quarkdown.pipeline.output.TextOutputArtifact
import eu.iamgio.quarkdown.template.TemplateProcessor

/**
 *
 */
class ProjectCreator(
    private val templateProcessorFactory: ProjectCreatorTemplateProcessorFactory,
    private val initialContentFactory: ProjectCreatorInitialContentSupplier,
    private val mainFileName: String,
) {
    fun createResources(): List<OutputResource> {
        val template: TemplateProcessor = templateProcessorFactory.create()

        // Initial content is processed via the same template processor.
        val initialContentCode =
            initialContentFactory.templateCodeContent
                ?.let { template.copy(text = it) }
                ?.process()

        template.optionalValue(ProjectCreatorTemplatePlaceholders.INITIAL_CONTENT, initialContentCode)

        val main =
            TextOutputArtifact(
                mainFileName,
                template.process().trim(),
                ArtifactType.QUARKDOWN,
            )

        return buildList {
            add(main)
            addAll(initialContentFactory.createResources())
        }
    }
}
