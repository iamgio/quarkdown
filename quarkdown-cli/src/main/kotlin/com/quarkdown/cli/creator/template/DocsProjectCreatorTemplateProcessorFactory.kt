package com.quarkdown.cli.creator.template

import com.quarkdown.core.document.DocumentInfo

/**
 * Implementation of [ProjectCreatorTemplateProcessorFactory] for `docs` projects,
 * which relies on [DefaultProjectCreatorTemplateProcessorFactory], but saved to `_setup.qd`.
 * @param info document information to inject into the template
 * @see DefaultProjectCreatorTemplateProcessorFactory
 */
class DocsProjectCreatorTemplateProcessorFactory(
    info: DocumentInfo,
) : ProjectCreatorTemplateProcessorFactory by DefaultProjectCreatorTemplateProcessorFactory(info) {
    override val targetFileName = "_setup"
}
