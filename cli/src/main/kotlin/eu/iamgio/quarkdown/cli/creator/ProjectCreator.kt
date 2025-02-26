package eu.iamgio.quarkdown.cli.creator

import eu.iamgio.quarkdown.pipeline.output.ArtifactType
import eu.iamgio.quarkdown.pipeline.output.OutputResource
import eu.iamgio.quarkdown.pipeline.output.TextOutputArtifact
import eu.iamgio.quarkdown.template.TemplateProcessor

private const val TEMPLATE = "/creator/main.qmd.template"

/**
 *
 */
class ProjectCreator(
    val name: String? = null,
) {
    fun createResources(): List<OutputResource> {
        val template =
            with(ProjectCreatorTemplatePlaceholders) {
                TemplateProcessor.fromResourceName(TEMPLATE).apply {
                    optionalValue(NAME, name)
                }
            }
        val main = TextOutputArtifact("main.qmd", template.process(), ArtifactType.QUARKDOWN)
        return listOf(main)
    }
}
