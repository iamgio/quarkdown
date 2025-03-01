package eu.iamgio.quarkdown.cli.creator.template

import eu.iamgio.quarkdown.template.TemplateProcessor

/**
 *
 */
interface ProjectCreatorTemplateProcessorFactory {
    fun create(): TemplateProcessor
}
