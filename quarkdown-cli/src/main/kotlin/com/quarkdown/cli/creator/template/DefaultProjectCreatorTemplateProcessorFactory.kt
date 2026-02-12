package com.quarkdown.cli.creator.template

import com.quarkdown.core.document.DocumentInfo
import com.quarkdown.core.document.DocumentType
import com.quarkdown.core.function.quarkdownName
import com.quarkdown.core.template.TemplateProcessor

private const val TEMPLATE = "/creator/main.qd.template"

/**
 * Implementation of [ProjectCreatorTemplateProcessorFactory]
 * based on the default template, which relies on document information
 * to fill placeholders.
 * @param info document information to inject into the template
 * @param template name of the template resource to use
 * @see ProjectCreatorTemplatePlaceholders
 */
class DefaultProjectCreatorTemplateProcessorFactory(
    private val info: DocumentInfo,
    private val template: String = TEMPLATE,
) : ProjectCreatorTemplateProcessorFactory {
    override fun create(): TemplateProcessor =
        with(ProjectCreatorTemplatePlaceholders) {
            TemplateProcessor.fromResourceName(template).apply {
                optionalValue(NAME, info.name)
                optionalValue(DESCRIPTION, info.description)
                conditional(KEYWORDS, info.keywords.isNotEmpty())
                iterable(KEYWORDS, info.keywords)
                conditional(AUTHORS, info.authors.isNotEmpty())
                iterable(AUTHORS, info.authors.map { it.name })
                optionalValue(TYPE, info.type.quarkdownName)
                conditional(IS_DOCS, info.type == DocumentType.DOCS)
                optionalValue(LANGUAGE, info.locale?.displayName)
                conditional(HAS_THEME, info.theme?.hasComponent == true)
                optionalValue(COLOR_THEME, info.theme?.color)
                optionalValue(LAYOUT_THEME, info.theme?.layout)
                conditional(USE_PAGE_COUNTER, info.type == DocumentType.PAGED)
            }
        }
}
