package eu.iamgio.quarkdown.cli.creator

import eu.iamgio.quarkdown.template.TemplateProcessor

/**
 *
 */
interface ProjectCreatorTemplateProcessorFactory {
    fun create(includeInitialContent: Boolean): TemplateProcessor
}
