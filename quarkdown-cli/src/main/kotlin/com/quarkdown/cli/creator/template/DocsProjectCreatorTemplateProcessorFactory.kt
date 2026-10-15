package com.quarkdown.cli.creator.template

import com.quarkdown.core.document.DocumentInfo
import com.quarkdown.template.TemplateProcessor

private const val TEMPLATE = "creator/docs/main.qd.jte"

/**
 * Implementation of [ProjectCreatorTemplateProcessorFactory] for `docs` projects,
 * which relies on [DefaultProjectCreatorTemplateProcessorFactory], but saved to `_setup.qd`,
 * plus an additional mapping for the main file that uses some example content.
 * @param info document information to inject into the template
 * @param includes references the setup file includes, one `.include` call each, so that every page receives them
 * @see DefaultProjectCreatorTemplateProcessorFactory
 */
class DocsProjectCreatorTemplateProcessorFactory(
    private val info: DocumentInfo,
    private val includes: List<String> = emptyList(),
) : ProjectCreatorTemplateProcessorFactory by DefaultProjectCreatorTemplateProcessorFactory(info, includes = includes) {
    override fun createFilenameMappings(): Map<String?, TemplateProcessor> =
        mapOf(
            "_setup" to create(),
            null to DefaultProjectCreatorTemplateProcessorFactory(info, TEMPLATE).create(),
        )
}
