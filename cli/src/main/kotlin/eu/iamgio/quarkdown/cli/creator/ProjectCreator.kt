package eu.iamgio.quarkdown.cli.creator

import eu.iamgio.quarkdown.document.DocumentType
import eu.iamgio.quarkdown.function.value.quarkdownName
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
    val author: String? = null,
    val type: DocumentType? = null,
) {
    private fun createTemplateProcessor() =
        with(ProjectCreatorTemplatePlaceholders) {
            TemplateProcessor.fromResourceName(TEMPLATE).apply {
                optionalValue(NAME, name)
                optionalValue(AUTHOR, author)
                optionalValue(TYPE, type?.quarkdownName)
            }
        }

    fun createResources(): List<OutputResource> {
        val template = this.createTemplateProcessor()

        val main =
            TextOutputArtifact(
                "main.qmd",
                template.process().trim(),
                ArtifactType.QUARKDOWN,
            )
        return listOf(main)
    }
}
