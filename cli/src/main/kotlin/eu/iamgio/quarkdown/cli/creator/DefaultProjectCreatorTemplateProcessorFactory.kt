package eu.iamgio.quarkdown.cli.creator

import eu.iamgio.quarkdown.document.DocumentInfo
import eu.iamgio.quarkdown.function.value.quarkdownName
import eu.iamgio.quarkdown.template.TemplateProcessor

private const val TEMPLATE = "/creator/main.qmd.template"

/**
 *
 */
class DefaultProjectCreatorTemplateProcessorFactory(
    private val info: DocumentInfo,
) : ProjectCreatorTemplateProcessorFactory {
    override fun create(): TemplateProcessor =
        with(ProjectCreatorTemplatePlaceholders) {
            TemplateProcessor.fromResourceName(TEMPLATE).apply {
                optionalValue(NAME, info.name)
                conditional(AUTHORS, info.authors.isNotEmpty())
                iterable(AUTHORS, info.authors.map { it.name })
                optionalValue(TYPE, info.type.quarkdownName)
                optionalValue(LANGUAGE, info.locale?.displayName)
                conditional(HAS_THEME, info.theme != null)
                optionalValue(COLOR_THEME, info.theme?.color)
                optionalValue(LAYOUT_THEME, info.theme?.layout)
            }
        }
}
