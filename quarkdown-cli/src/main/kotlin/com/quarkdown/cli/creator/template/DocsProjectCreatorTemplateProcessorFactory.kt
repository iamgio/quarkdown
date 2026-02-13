package com.quarkdown.cli.creator.template

import com.quarkdown.core.document.DocumentInfo
import com.quarkdown.core.template.TemplateProcessor

private const val TEMPLATE = "/creator/docs/main.qd.kte"

/**
 * Implementation of [ProjectCreatorTemplateProcessorFactory] for `docs` projects,
 * which relies on [DefaultProjectCreatorTemplateProcessorFactory], but saved to `_setup.qd`,
 * plus an additional mapping for the main file that uses some example content.
 * @param info document information to inject into the template
 * @see DefaultProjectCreatorTemplateProcessorFactory
 */
class DocsProjectCreatorTemplateProcessorFactory(
    private val info: DocumentInfo,
) : ProjectCreatorTemplateProcessorFactory by DefaultProjectCreatorTemplateProcessorFactory(info) {
    override fun createFilenameMappings(): Map<String?, TemplateProcessor> =
        mapOf(
            "_setup" to create(),
            null to DefaultProjectCreatorTemplateProcessorFactory(info, TEMPLATE).create(),
        )
}
