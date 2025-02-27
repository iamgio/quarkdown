package eu.iamgio.quarkdown.cli.creator

import eu.iamgio.quarkdown.document.DocumentInfo
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
    private val info: DocumentInfo,
) {
    private fun createTemplateProcessor() =
        with(ProjectCreatorTemplatePlaceholders) {
            TemplateProcessor.fromResourceName(TEMPLATE).apply {
                optionalValue(NAME, info.name)
                conditional(AUTHORS, info.authors.isNotEmpty())
                iterable(AUTHORS, info.authors.map { it.name })
                optionalValue(TYPE, info.type.quarkdownName)
                optionalValue(LANGUAGE, info.locale?.displayName)
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
